package com.tudominio.checklistapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tudominio.checklistapp.ChecklistApplication
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.data.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

/**
 * Equipment types available for inspection
 */
enum class EquipmentType(val displayName: String) {
    CAEX_797F("CAEX 797F"),
    CAEX_798AC("CAEX 798AC")
}

/**
 * ViewModel that manages the state and logic of the New Inspection screen.
 */
class NewInspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NewInspectionViewModel"

    // Repository instance from the Application class
    private val repository = try {
        (application as? ChecklistApplication)?.repository
    } catch (e: Exception) {
        Log.e(TAG, "Error getting repository: ${e.message}", e)
        null
    }

    // Current inspection state
    var inspection by mutableStateOf(Inspection())
        private set

    // Equipment type and number state
    var selectedEquipmentType by mutableStateOf(EquipmentType.CAEX_797F)
        private set

    var equipmentNumber by mutableStateOf("")
        private set

    // Screen state
    var isLoading by mutableStateOf(false)
        private set

    // Save state
    var isSaving by mutableStateOf(false)
        private set

    // Success or error state
    var saveSuccess by mutableStateOf<Boolean?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Current inspection stage
    var currentStage by mutableStateOf(InspectionStage.INITIAL_INFO)
        private set

    // Field error states
    var equipmentError by mutableStateOf(false)
        private set
    var inspectorError by mutableStateOf(false)
        private set
    var supervisorError by mutableStateOf(false)
        private set
    var horometerError by mutableStateOf(false)
        private set

    // Current item index
    var currentItemIndex by mutableStateOf(0)
        private set

    /**
     * Updates the equipment type
     */
    fun updateEquipmentType(type: EquipmentType) {
        selectedEquipmentType = type
        // Update the formatted equipment ID
        updateFormattedEquipment()
    }

    /**
     * Updates the equipment number and validates it
     */
    fun updateEquipmentNumber(value: String) {
        equipmentNumber = value
        equipmentError = value.isBlank()
        // Update the formatted equipment ID
        updateFormattedEquipment()
    }

    /**
     * Updates the formatted equipment field in the inspection
     */
    private fun updateFormattedEquipment() {
        val formattedEquipment = if (equipmentNumber.isNotBlank()) {
            "${selectedEquipmentType.displayName} $equipmentNumber"
        } else {
            ""
        }
        inspection = inspection.copy(equipment = formattedEquipment)
    }

    /**
     * Updates the inspector field and validates it
     */
    fun updateInspector(value: String) {
        inspection = inspection.copy(inspector = value)
        inspectorError = value.isBlank()
    }

    /**
     * Updates the supervisor field and validates it
     */
    fun updateSupervisor(value: String) {
        inspection = inspection.copy(supervisor = value)
        supervisorError = value.isBlank()
    }

    /**
     * Updates the horometer field and validates it
     */
    fun updateHorometer(value: String) {
        inspection = inspection.copy(horometer = value)
        horometerError = value.isBlank()
    }

    /**
     * Validates if all initial fields are complete
     * @return true if all required fields are complete, false otherwise
     */
    fun validateInitialFields(): Boolean {
        equipmentError = equipmentNumber.isBlank()
        inspectorError = inspection.inspector.isBlank()
        supervisorError = inspection.supervisor.isBlank()
        horometerError = inspection.horometer.isBlank()

        return !equipmentError && !inspectorError && !supervisorError && !horometerError
    }

    /**
     * Advances to the next stage of the inspection process
     */
    fun proceedToNextStage() {
        when (currentStage) {
            InspectionStage.INITIAL_INFO -> {
                if (validateInitialFields()) {
                    // Only proceed if all fields are complete
                    loadChecklistItems()
                    currentStage = InspectionStage.CHECKLIST
                }
            }
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex < inspection.items.size - 1) {
                    // Advance to the next checklist item
                    currentItemIndex++
                } else {
                    // If we've completed all items, advance to summary
                    currentStage = InspectionStage.SUMMARY
                }
            }
            InspectionStage.SUMMARY -> {
                // Finalize the inspection
                saveInspection()
                currentStage = InspectionStage.COMPLETED
            }
            InspectionStage.COMPLETED -> {
                // No more stages
            }
        }
    }

    /**
     * Loads checklist items for the inspection with real data
     */
    private fun loadChecklistItems() {
        isLoading = true

        // Create items with their respective questions based on equipment type
        val items = when (selectedEquipmentType) {
            EquipmentType.CAEX_797F -> loadCAEX797FItems()
            EquipmentType.CAEX_798AC -> loadCAEX798ACItems()
        }

        inspection = inspection.copy(items = items)
        isLoading = false
    }

    /**
     * Loads checklist items specific to CAEX 797F
     */
    private fun loadCAEX797FItems(): List<InspectionItem> {
        return listOf(
            // Item 1: Condiciones Generales
            createItem(
                id = "item1",
                name = "Condiciones Generales",
                questions = listOf(
                    "Extintores contra incendio habilitados en plataforma cabina operador y con inspección al día",
                    "Pulsador parada de emergencia en buen estado",
                    "Verificar desgaste excesivo y falta de pernos del aro",
                    "Inspección visual y al día del sistema AFEX / ANSUR",
                    "Pasadores de tolva",
                    "Fugas sistemas hidráulicos puntos calientes (Motor)",
                    "Números de identificación caex instalados (frontal, trasero)",
                    "Estanque de combustible sin fugas",
                    "Estanque de aceite hidráulico sin fugas",
                    "Sistema engrase llega a todos los puntos"
                )
            ),

            // Item 2: Cabina Operador
            createItem(
                id = "item2",
                name = "Cabina Operador",
                questions = listOf(
                    "Panel de alarmas en buen estado",
                    "Asiento operador y de copiloto en buen estado (chequear cinturón de seguridad en ambos asientos, apoya brazos, riel de desplazamiento, pulmón de aire)",
                    "Espejos en buen estado, sin rayaduras",
                    "Revisar bitácora del equipo (dejar registro)",
                    "Radio musical y parlantes en buen estado",
                    "Testigo indicador viraje funcionando (intermitente)",
                    "Funcionamiento bocina",
                    "Funcionamiento limpia parabrisas",
                    "Funcionamiento alza vidrios",
                    "Funcionamiento de A/C",
                    "Parasol en buen estado"
                )
            ),

            // Item 3: Sistema de Dirección
            createItem(
                id = "item3",
                name = "Sistema de Dirección",
                questions = listOf(
                    "Barra de dirección en buen estado",
                    "Fugas de aceite por bombas/cañerías / mangueras / conectores",
                    "Cilindros de dirección sin fugas de aceite / sin daños"
                )
            ),

            // Item 4: Sistema de frenos
            createItem(
                id = "item4",
                name = "Sistema de frenos",
                questions = listOf(
                    "Fugas de aceite por cañerías / mangueras / conectores",
                    "Gabinete hidráulico sin fugas de aceite"
                )
            ),

            // Item 5: Motor Diesel
            createItem(
                id = "item5",
                name = "Motor Diesel",
                questions = listOf(
                    "Fugas de aceite por cañerías / mangueras / conectores",
                    "Fugas de combustibles por cañerías / mangueras / turbos / carter",
                    "Fugas de refrigerante",
                    "Mangueras con roce y/o sueltas",
                    "Cables eléctricos sin roce y ruteados bajo estándar",
                    "Boquillas sistema AFEX bien direccionadas"
                )
            ),

            // Item 6: Suspensiones delanteras
            createItem(
                id = "item6",
                name = "Suspensiones delanteras",
                questions = listOf(
                    "Estado de sello protector vástago (altura susp.)",
                    "Fugas de aceite o grasa"
                )
            ),

            // Item 7: Suspensiones traseras
            createItem(
                id = "item7",
                name = "Suspensiones traseras",
                questions = listOf(
                    "Suspensión izquierda con pasador desplazado",
                    "Suspensión derecha con pasador desplazado",
                    "Articulaciones lubricadas"
                )
            ),

            // Item 8: Sistema estructural
            createItem(
                id = "item8",
                name = "Sistema estructural",
                questions = listOf(
                    "Baranda o cadena acceso a escalas emergencia",
                    "Barandas plataforma cabina operador",
                    "Barandas escalera de acceso",
                    "Escalera de acceso flotante"
                )
            )
        )
    }

    /**
     * Loads checklist items specific to CAEX 798AC
     */
    private fun loadCAEX798ACItems(): List<InspectionItem> {
        return listOf(
            // Sample items for 798AC - to be replaced with real questions later
            createItem(
                id = "item1",
                name = "Sistema Eléctrico 798AC",
                questions = listOf(
                    "Panel de control principal en buen estado",
                    "Conexiones eléctricas sin daños visibles",
                    "Sistema de iluminación funciona correctamente",
                    "Baterías y conexiones en buen estado"
                )
            ),
            createItem(
                id = "item2",
                name = "Sistema Hidráulico 798AC",
                questions = listOf(
                    "Mangueras hidráulicas sin fugas",
                    "Nivel de aceite hidráulico correcto",
                    "Bombas hidráulicas sin ruidos anormales",
                    "Filtros hidráulicos limpios y en buen estado"
                )
            ),
            createItem(
                id = "item3",
                name = "Sistema de Propulsión 798AC",
                questions = listOf(
                    "Motor principal en buen estado",
                    "Sistema de transmisión sin ruidos anormales",
                    "Convertidor de par funciona correctamente",
                    "Frenos de servicio operativos"
                )
            ),
            createItem(
                id = "item4",
                name = "Sistema de Frenos 798AC",
                questions = listOf(
                    "Frenos en buen estado",
                    "Sistema antibloqueo funcional",
                    "Sin fugas en el sistema de frenos"
                )
            ),
            createItem(
                id = "item5",
                name = "Sistema de Suspensión 798AC",
                questions = listOf(
                    "Suspensiones en buen estado",
                    "Sin fugas de aceite en amortiguadores",
                    "Sin ruidos anormales al operar"
                )
            ),
            createItem(
                id = "item6",
                name = "Sistema de Control 798AC",
                questions = listOf(
                    "Dispositivos de control funcionando correctamente",
                    "Pantallas operativas sin errores",
                    "Sensores calibrados y operativos"
                )
            ),
            createItem(
                id = "item7",
                name = "Equipo de Seguridad 798AC",
                questions = listOf(
                    "Extintores en buen estado y vigentes",
                    "Sistema de parada de emergencia funcionando",
                    "Alarmas y bocinas operativas"
                )
            ),
            createItem(
                id = "item8",
                name = "Estructura General 798AC",
                questions = listOf(
                    "Sin daños visibles en estructura",
                    "Sin fisuras en componentes críticos",
                    "Barandas de seguridad intactas"
                )
            )
        )
    }

    /**
     * Creates an inspection item with its questions
     */
    private fun createItem(id: String, name: String, questions: List<String>): InspectionItem {
        return InspectionItem(
            id = id,
            name = name,
            questions = questions.mapIndexed { index, questionText ->
                InspectionQuestion(
                    id = "${id}_q${index + 1}",
                    text = questionText
                )
            }
        )
    }

    /**
     * Saves the completed inspection to the database
     */
    private fun saveInspection() {
        isSaving = true
        saveSuccess = null
        errorMessage = null

        viewModelScope.launch {
            try {
                // Log start of saving process
                Log.d(TAG, "Starting to save inspection ${inspection.id}")

                // Mark the inspection as completed
                inspection = inspection.copy(isCompleted = true)

                // Check if repository is available
                if (repository == null) {
                    Log.e(TAG, "Repository is null, cannot save inspection")
                    saveSuccess = false
                    errorMessage = "No se pudo acceder al repositorio de datos."
                    isSaving = false
                    return@launch
                }

                // Save to the database using the repository
                val result = withContext(Dispatchers.IO) {
                    try {
                        Log.d(TAG, "Calling repository.saveInspection...")
                        val success = repository.saveInspection(inspection)
                        Log.d(TAG, "Save operation result: $success")
                        success
                    } catch (e: Exception) {
                        Log.e(TAG, "Database error while saving: ${e.message}", e)
                        false
                    }
                }

                saveSuccess = result
                if (!result) {
                    Log.e(TAG, "Save operation returned false")
                    errorMessage = "No se pudo guardar la inspección en la base de datos."
                } else {
                    Log.d(TAG, "Inspection saved successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving inspection: ${e.message}", e)
                saveSuccess = false
                errorMessage = "Error: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    /**
     * Updates the answer to a specific question
     *
     * @param question The question being updated
     * @param answer The new answer for the question
     */
    fun updateQuestionAnswer(question: InspectionQuestion, answer: Answer) {
        // First, get the current item based on the index
        if (currentItemIndex < 0 || currentItemIndex >= inspection.items.size) {
            return // Invalid item index
        }

        val currentItems = inspection.items.toMutableList()
        val currentItem = currentItems[currentItemIndex]

        // Find the specific question within the current item
        val questions = currentItem.questions.toMutableList()
        val questionIndex = questions.indexOfFirst { it.id == question.id }

        if (questionIndex != -1) {
            // If the question already had an answer, keep its photos
            val existingPhotos = questions[questionIndex].answer?.photos ?: emptyList()

            // Only update the photos if the new answer doesn't include them
            val updatedAnswer = if (answer.photos.isEmpty()) {
                answer.copy(photos = existingPhotos)
            } else {
                answer
            }

            // Update the question with the new answer
            questions[questionIndex] = questions[questionIndex].copy(answer = updatedAnswer)

            // Create a new item with the updated questions
            val updatedItem = currentItem.copy(questions = questions)

            // Update the items list
            currentItems[currentItemIndex] = updatedItem

            // Update the complete inspection
            inspection = inspection.copy(items = currentItems)
        }
    }

    /**
     * Adds a photo to a specific question
     *
     * @param questionId ID of the question to add the photo to
     * @param photoUri URI of the photo taken
     */
    fun addPhotoToQuestion(questionId: String, photoUri: String) {
        val question = getQuestionById(questionId) ?: return

        // Create a new Photo object
        val newPhoto = Photo(
            uri = photoUri,
            timestamp = LocalDateTime.now()
        )

        // Get the current answer or create a new one if it doesn't exist
        val currentAnswer = question.answer ?: Answer(isConform = false)

        // Add the new photo to the existing list
        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            add(newPhoto)
        }

        // Create an updated answer with the new photo
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Update the question with the new answer
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Removes a photo from a specific question
     *
     * @param questionId ID of the question to remove the photo from
     * @param photo The photo to remove
     */
    fun removePhotoFromQuestion(questionId: String, photo: Photo) {
        val question = getQuestionById(questionId) ?: return

        // Get the current answer
        val currentAnswer = question.answer ?: return

        // Remove the photo from the list
        val updatedPhotos = currentAnswer.photos.filter { it.id != photo.id }

        // Create an updated answer without the removed photo
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Update the question with the new answer
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Updates a photo with drawings
     *
     * @param questionId ID of the question containing the photo
     * @param photoId ID of the photo to update
     * @param drawingUri URI of the image with drawings
     */
    fun updatePhotoWithDrawing(questionId: String, photoId: String, drawingUri: String) {
        val question = getQuestionById(questionId) ?: return

        // Get the current answer
        val currentAnswer = question.answer ?: return

        // Find the photo to update
        val photoIndex = currentAnswer.photos.indexOfFirst { it.id == photoId }
        if (photoIndex == -1) return

        // Get the current photo
        val photo = currentAnswer.photos[photoIndex]

        // Create a new photo with the drawing information
        val updatedPhoto = photo.copy(
            hasDrawings = true,
            drawingUri = drawingUri
        )

        // Update the photos list
        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            set(photoIndex, updatedPhoto)
        }

        // Create an updated answer with the modified photo
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Update the question with the new answer
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Updates a question with a new answer
     *
     * @param questionId ID of the question to update
     * @param answer The new answer
     */
    private fun updateQuestionWithAnswer(questionId: String, answer: Answer) {
        // Find the item and question
        val items = inspection.items.toMutableList()

        for (i in items.indices) {
            val item = items[i]
            val questions = item.questions.toMutableList()

            val questionIndex = questions.indexOfFirst { it.id == questionId }
            if (questionIndex != -1) {
                // Update the question with the new answer
                questions[questionIndex] = questions[questionIndex].copy(answer = answer)

                // Update the item with the updated questions
                items[i] = item.copy(questions = questions)

                // Update the complete inspection
                inspection = inspection.copy(items = items)
                return
            }
        }
    }

    /**
     * Finds a question by its ID
     *
     * @param questionId ID of the question to find
     * @return The question found or null if it doesn't exist
     */
    fun getQuestionById(questionId: String): InspectionQuestion? {
        inspection.items.forEach { item ->
            item.questions.forEach { question ->
                if (question.id == questionId) {
                    return question
                }
            }
        }
        return null
    }

    /**
     * Goes back to the previous stage in the inspection process
     */
    fun goBack(): Boolean {
        return when (currentStage) {
            InspectionStage.INITIAL_INFO -> false // No previous step
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex > 0) {
                    // Go back to the previous item
                    currentItemIndex--
                    true
                } else {
                    // Return to initial information
                    currentStage = InspectionStage.INITIAL_INFO
                    true
                }
            }
            InspectionStage.SUMMARY -> {
                // Return to the last checklist item
                currentStage = InspectionStage.CHECKLIST
                currentItemIndex = inspection.items.size - 1
                true
            }
            InspectionStage.COMPLETED -> {
                // Return to summary
                currentStage = InspectionStage.SUMMARY
                true
            }
        }
    }

    /**
     * Resets the inspection to its initial state
     * Useful when starting a new inspection
     */
    fun resetInspection() {
        inspection = Inspection()
        equipmentNumber = ""
        selectedEquipmentType = EquipmentType.CAEX_797F
        currentStage = InspectionStage.INITIAL_INFO
        currentItemIndex = 0
        equipmentError = false
        inspectorError = false
        supervisorError = false
        horometerError = false
        isLoading = false
        isSaving = false
        saveSuccess = null
        errorMessage = null
    }

    /**
     * Clears the error message
     */
    fun clearError() {
        errorMessage = null
    }
}