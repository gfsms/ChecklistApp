package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.ChecklistApplication
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.data.model.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

/**
 * Enumeration for the types of findings in post-inspection
 */
enum class FindingType {
    REPEATED,      // Finding that was also present in control inspection
    POST_INTERVENTION // New finding discovered in post-inspection
}

/**
 * ViewModel for the post-intervention equipment delivery inspection process
 */
class PostInspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "PostInspectionViewModel"

    // Repository instance from the Application class
    private val repository = try {
        (application as? ChecklistApplication)?.repository
    } catch (e: Exception) {
        Log.e(TAG, "Error getting repository: ${e.message}", e)
        null
    }

    // Control inspections state
    private val _controlInspections = MutableStateFlow<List<Inspection>>(emptyList())
    val controlInspections: StateFlow<List<Inspection>> = _controlInspections.asStateFlow()

    // Filtered inspections based on search
    var filteredInspections by mutableStateOf<List<Inspection>>(emptyList())
        private set

    // Selected control inspection
    var selectedControlInspection by mutableStateOf<Inspection?>(null)
        private set

    // Post-intervention inspection state
    var postInspection by mutableStateOf(Inspection())
        private set

    // Loading state
    var isLoading by mutableStateOf(false)
        private set

    // Saving state
    var isSaving by mutableStateOf(false)
        private set
    var saveSuccess by mutableStateOf<Boolean?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Current inspection stage
    var currentStage by mutableStateOf(InspectionStage.INITIAL_INFO)
        private set

    // Current item index
    var currentItemIndex by mutableStateOf(0)
        private set

    // Search query for filtering
    var searchQuery by mutableStateOf("")
        private set

    // Map to track finding types
    private val findingTypeMap = mutableMapOf<String, FindingType>()

    /**
     * Loads all control inspections
     */
    fun loadControlInspections() {
        viewModelScope.launch {
            isLoading = true
            try {
                repository?.getAllInspectionsList()?.let { inspections ->
                    // Only include completed inspections
                    val controlInspections = inspections.filter { it.isCompleted }
                    _controlInspections.value = controlInspections
                    applySearchFilter()
                } ?: run {
                    Log.e(TAG, "Repository is null, cannot load inspections")
                    errorMessage = "No se pudo acceder al repositorio"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading control inspections: ${e.message}", e)
                errorMessage = "Error cargando inspecciones: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Updates search query and filters inspections
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        applySearchFilter()
    }

    /**
     * Applies search filter to control inspections
     */
    private fun applySearchFilter() {
        filteredInspections = if (searchQuery.isEmpty()) {
            _controlInspections.value
        } else {
            _controlInspections.value.filter { inspection ->
                inspection.equipment.contains(searchQuery, ignoreCase = true) ||
                        inspection.inspector.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    /**
     * Initializes a post-intervention inspection based on control inspection
     */
    fun initializePostInspection(controlInspectionId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                repository?.getFullInspection(controlInspectionId)?.let { controlInspection ->
                    selectedControlInspection = controlInspection

                    // Create new inspection based on control inspection
                    postInspection = Inspection(
                        id = UUID.randomUUID().toString(),
                        equipment = controlInspection.equipment,
                        inspector = "",
                        supervisor = "",
                        horometer = "",
                        date = LocalDateTime.now(),
                        items = createPostInspectionItems(controlInspection),
                        isCompleted = false
                    )

                    // Reset stage and index
                    currentStage = InspectionStage.INITIAL_INFO
                    currentItemIndex = 0
                } ?: run {
                    Log.e(TAG, "Control inspection not found")
                    errorMessage = "No se encontró la inspección de control"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing post inspection: ${e.message}", e)
                errorMessage = "Error iniciando inspección: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Creates items for post-inspection based on control inspection
     */
    private fun createPostInspectionItems(controlInspection: Inspection): List<InspectionItem> {
        return controlInspection.items.map { controlItem ->
            // Create a new item with the same name but new ID
            InspectionItem(
                id = UUID.randomUUID().toString(),
                name = controlItem.name,
                questions = controlItem.questions.map { controlQuestion ->
                    // Create a new question for each control question
                    val newQuestion = InspectionQuestion(
                        id = UUID.randomUUID().toString(),
                        text = controlQuestion.text,
                        answer = null
                    )

                    // Track if this question had a finding in control inspection
                    if (controlQuestion.answer?.isConform == false) {
                        findingTypeMap[newQuestion.id] = FindingType.REPEATED
                    }

                    newQuestion
                }
            )
        }
    }

    /**
     * Updates the inspector field
     */
    fun updateInspector(value: String) {
        postInspection = postInspection.copy(inspector = value)
    }

    /**
     * Updates the supervisor field
     */
    fun updateSupervisor(value: String) {
        postInspection = postInspection.copy(supervisor = value)
    }

    /**
     * Updates the horometer field
     */
    fun updateHorometer(value: String) {
        postInspection = postInspection.copy(horometer = value)
    }

    /**
     * Validates initial fields
     */
    fun validateInitialFields(): Boolean {
        return postInspection.inspector.isNotBlank() &&
                postInspection.supervisor.isNotBlank() &&
                postInspection.horometer.isNotBlank()
    }

    /**
     * Advances to the next stage of inspection
     */
    fun proceedToNextStage() {
        when (currentStage) {
            InspectionStage.INITIAL_INFO -> {
                if (validateInitialFields()) {
                    currentStage = InspectionStage.CHECKLIST
                }
            }
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex < postInspection.items.size - 1) {
                    currentItemIndex++
                } else {
                    currentStage = InspectionStage.SUMMARY
                }
            }
            InspectionStage.SUMMARY -> {
                savePostInspection()
                currentStage = InspectionStage.COMPLETED
            }
            InspectionStage.COMPLETED -> {
                // No more stages
            }
        }
    }

    /**
     * Goes back to previous stage
     */
    fun goBack(): Boolean {
        return when (currentStage) {
            InspectionStage.INITIAL_INFO -> false
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex > 0) {
                    currentItemIndex--
                    true
                } else {
                    currentStage = InspectionStage.INITIAL_INFO
                    true
                }
            }
            InspectionStage.SUMMARY -> {
                currentStage = InspectionStage.CHECKLIST
                currentItemIndex = postInspection.items.size - 1
                true
            }
            InspectionStage.COMPLETED -> {
                currentStage = InspectionStage.SUMMARY
                true
            }
        }
    }

    /**
     * Updates a question's answer in the post-inspection
     */
    fun updateQuestionAnswer(question: InspectionQuestion, answer: Answer) {
        if (currentItemIndex < 0 || currentItemIndex >= postInspection.items.size) {
            return
        }

        val items = postInspection.items.toMutableList()
        val currentItem = items[currentItemIndex]
        val questions = currentItem.questions.toMutableList()
        val questionIndex = questions.indexOfFirst { it.id == question.id }

        if (questionIndex != -1) {
            // If question already had an answer, preserve photos
            val existingPhotos = questions[questionIndex].answer?.photos ?: emptyList()
            val updatedAnswer = if (answer.photos.isEmpty()) {
                answer.copy(photos = existingPhotos)
            } else {
                answer
            }

            // If this is a new finding (not present in control inspection), mark it
            if (!findingTypeMap.containsKey(question.id) && !answer.isConform) {
                findingTypeMap[question.id] = FindingType.POST_INTERVENTION
            }

            // Update the question with new answer
            questions[questionIndex] = questions[questionIndex].copy(answer = updatedAnswer)

            // Update items list
            items[currentItemIndex] = currentItem.copy(questions = questions)

            // Update inspection
            postInspection = postInspection.copy(items = items)
        }
    }

    /**
     * Gets a question by ID
     */
    fun getQuestionById(questionId: String): InspectionQuestion? {
        postInspection.items.forEach { item ->
            item.questions.forEach { question ->
                if (question.id == questionId) {
                    return question
                }
            }
        }
        return null
    }

    /**
     * Checks if a question had a finding in control inspection
     */
    fun wasControlFinding(questionId: String): Boolean {
        return findingTypeMap[questionId] == FindingType.REPEATED
    }

    /**
     * Gets the finding type for a question
     */
    fun getFindingType(questionId: String): FindingType? {
        return findingTypeMap[questionId]
    }

    /**
     * Adds a photo to a question
     */
    fun addPhotoToQuestion(questionId: String, photoUri: String) {
        val question = getQuestionById(questionId) ?: return

        val newPhoto = Photo(
            uri = photoUri,
            timestamp = LocalDateTime.now()
        )

        val currentAnswer = question.answer ?: Answer(isConform = false)
        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            add(newPhoto)
        }

        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Removes a photo from a question
     */
    fun removePhotoFromQuestion(questionId: String, photo: Photo) {
        val question = getQuestionById(questionId) ?: return
        val currentAnswer = question.answer ?: return

        val updatedPhotos = currentAnswer.photos.filter { it.id != photo.id }
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Updates a photo with drawing
     */
    fun updatePhotoWithDrawing(questionId: String, photoId: String, drawingUri: String) {
        val question = getQuestionById(questionId) ?: return
        val currentAnswer = question.answer ?: return

        val photoIndex = currentAnswer.photos.indexOfFirst { it.id == photoId }
        if (photoIndex == -1) return

        val photo = currentAnswer.photos[photoIndex]
        val updatedPhoto = photo.copy(
            hasDrawings = true,
            drawingUri = drawingUri
        )

        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            set(photoIndex, updatedPhoto)
        }

        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Updates a question with a new answer
     */
    private fun updateQuestionWithAnswer(questionId: String, answer: Answer) {
        val items = postInspection.items.toMutableList()

        for (i in items.indices) {
            val item = items[i]
            val questions = item.questions.toMutableList()

            val questionIndex = questions.indexOfFirst { it.id == questionId }
            if (questionIndex != -1) {
                questions[questionIndex] = questions[questionIndex].copy(answer = answer)
                items[i] = item.copy(questions = questions)
                postInspection = postInspection.copy(items = items)
                return
            }
        }
    }

    /**
     * Saves the post-intervention inspection
     */
    private fun savePostInspection() {
        isSaving = true
        saveSuccess = null
        errorMessage = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to save post-inspection ${postInspection.id}")

                // Mark inspection as completed
                postInspection = postInspection.copy(isCompleted = true)

                if (repository == null) {
                    Log.e(TAG, "Repository is null, cannot save inspection")
                    saveSuccess = false
                    errorMessage = "No se pudo acceder al repositorio de datos."
                    isSaving = false
                    return@launch
                }

                // Save using repository
                val result = repository.saveInspection(postInspection)

                saveSuccess = result
                if (!result) {
                    Log.e(TAG, "Save operation returned false")
                    errorMessage = "No se pudo guardar la inspección en la base de datos."
                } else {
                    Log.d(TAG, "Post-inspection saved successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving post-inspection: ${e.message}", e)
                saveSuccess = false
                errorMessage = "Error: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    /**
     * Resets the view model state
     */
    fun reset() {
        postInspection = Inspection()
        selectedControlInspection = null
        currentStage = InspectionStage.INITIAL_INFO
        currentItemIndex = 0
        findingTypeMap.clear()
        saveSuccess = null
        errorMessage = null
    }
}