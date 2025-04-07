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
 * ViewModel que gestiona el estado y la lógica de la pantalla de Nueva Inspección.
 * Se encarga de mantener el estado del formulario, validar los campos y
 * gestionar la navegación entre las diferentes etapas del proceso de inspección.
 */
class NewInspectionViewModel(application: Application) : ViewModel() {
    private val TAG = "NewInspectionViewModel"

    // Repository instance from the Application class
    private val repository = try {
        (application as? ChecklistApplication)?.repository
    } catch (e: Exception) {
        Log.e(TAG, "Error getting repository: ${e.message}", e)
        null
    }

    // Estado de la inspección actual
    var inspection by mutableStateOf(Inspection())
        private set

    // Estado de la pantalla
    var isLoading by mutableStateOf(false)
        private set

    // Estado de guardado
    var isSaving by mutableStateOf(false)
        private set

    // Estado de éxito o error de guardado
    var saveSuccess by mutableStateOf<Boolean?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Etapa actual del proceso de inspección
    var currentStage by mutableStateOf(InspectionStage.INITIAL_INFO)
        private set

    // Estado de errores en los campos
    var equipmentError by mutableStateOf(false)
        private set
    var inspectorError by mutableStateOf(false)
        private set
    var supervisorError by mutableStateOf(false)
        private set
    var horometerError by mutableStateOf(false)
        private set

    // Índice del ítem actual en el checklist
    var currentItemIndex by mutableStateOf(0)
        private set

    /**
     * Actualiza el campo equipo y valida su contenido.
     */
    fun updateEquipment(value: String) {
        inspection = inspection.copy(equipment = value)
        equipmentError = value.isBlank()
    }

    /**
     * Actualiza el campo inspector y valida su contenido.
     */
    fun updateInspector(value: String) {
        inspection = inspection.copy(inspector = value)
        inspectorError = value.isBlank()
    }

    /**
     * Actualiza el campo supervisor y valida su contenido.
     */
    fun updateSupervisor(value: String) {
        inspection = inspection.copy(supervisor = value)
        supervisorError = value.isBlank()
    }

    /**
     * Actualiza el campo horómetro y valida su contenido.
     */
    fun updateHorometer(value: String) {
        inspection = inspection.copy(horometer = value)
        horometerError = value.isBlank()
    }

    /**
     * Valida si todos los campos iniciales están completos.
     * @return true si todos los campos necesarios están completos, false en caso contrario.
     */
    fun validateInitialFields(): Boolean {
        equipmentError = inspection.equipment.isBlank()
        inspectorError = inspection.inspector.isBlank()
        supervisorError = inspection.supervisor.isBlank()
        horometerError = inspection.horometer.isBlank()

        return !equipmentError && !inspectorError && !supervisorError && !horometerError
    }

    /**
     * Avanza a la siguiente etapa del proceso de inspección.
     */
    fun proceedToNextStage() {
        when (currentStage) {
            InspectionStage.INITIAL_INFO -> {
                if (validateInitialFields()) {
                    // Solo avanzamos si todos los campos están completos
                    loadChecklistItems()
                    currentStage = InspectionStage.CHECKLIST
                }
            }
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex < inspection.items.size - 1) {
                    // Avanzar al siguiente ítem del checklist
                    currentItemIndex++
                } else {
                    // Si hemos completado todos los ítems, avanzamos al resumen
                    currentStage = InspectionStage.SUMMARY
                }
            }
            InspectionStage.SUMMARY -> {
                // Finalizar la inspección
                saveInspection()
                currentStage = InspectionStage.COMPLETED
            }
            InspectionStage.COMPLETED -> {
                // No hay más etapas
            }
        }
    }

    /**
     * Carga los ítems del checklist para la inspección con datos reales.
     */
    private fun loadChecklistItems() {
        isLoading = true

        // Crear los 8 ítems con sus respectivas preguntas
        val items = listOf(
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

        inspection = inspection.copy(items = items)
        isLoading = false
    }

    /**
     * Crea un ítem de inspección con sus preguntas.
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
     * Guarda la inspección completada en la base de datos.
     */
    private fun saveInspection() {
        isSaving = true
        saveSuccess = null
        errorMessage = null

        viewModelScope.launch {
            try {
                // Mark the inspection as completed
                inspection = inspection.copy(isCompleted = true)

                // Check if repository is available
                if (repository == null) {
                    saveSuccess = false
                    errorMessage = "No se pudo acceder al repositorio de datos."
                    return@launch
                }

                // Save to the database using the repository
                val result = withContext(Dispatchers.IO) {
                    repository.saveInspection(inspection)
                }

                saveSuccess = result
                if (!result) {
                    errorMessage = "No se pudo guardar la inspección en la base de datos."
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
     * Actualiza la respuesta de una pregunta específica.
     *
     * @param question La pregunta que se está actualizando
     * @param answer La nueva respuesta para la pregunta
     */
    fun updateQuestionAnswer(question: InspectionQuestion, answer: Answer) {
        // Primero, obtenemos el ítem actual basado en el índice
        if (currentItemIndex < 0 || currentItemIndex >= inspection.items.size) {
            return // Índice de ítem inválido
        }

        val currentItems = inspection.items.toMutableList()
        val currentItem = currentItems[currentItemIndex]

        // Buscamos la pregunta específica dentro del ítem actual
        val questions = currentItem.questions.toMutableList()
        val questionIndex = questions.indexOfFirst { it.id == question.id }

        if (questionIndex != -1) {
            // Si la pregunta ya tenía respuesta, conservamos sus fotos
            val existingPhotos = questions[questionIndex].answer?.photos ?: emptyList()

            // Solo actualizamos las fotos si la nueva respuesta no las incluye
            val updatedAnswer = if (answer.photos.isEmpty()) {
                answer.copy(photos = existingPhotos)
            } else {
                answer
            }

            // Actualizamos la pregunta con la nueva respuesta
            questions[questionIndex] = questions[questionIndex].copy(answer = updatedAnswer)

            // Creamos un nuevo ítem con las preguntas actualizadas
            val updatedItem = currentItem.copy(questions = questions)

            // Actualizamos la lista de ítems
            currentItems[currentItemIndex] = updatedItem

            // Actualizamos la inspección completa
            inspection = inspection.copy(items = currentItems)
        }
    }

    /**
     * Añade una foto a una pregunta específica.
     *
     * @param questionId ID de la pregunta a la que se añade la foto
     * @param photoUri URI de la foto tomada
     */
    fun addPhotoToQuestion(questionId: String, photoUri: String) {
        val question = getQuestionById(questionId) ?: return

        // Creamos un nuevo objeto Photo
        val newPhoto = Photo(
            uri = photoUri,
            timestamp = LocalDateTime.now()
        )

        // Obtenemos la respuesta actual o creamos una nueva si no existe
        val currentAnswer = question.answer ?: Answer(isConform = false)

        // Añadimos la nueva foto a la lista existente
        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            add(newPhoto)
        }

        // Creamos una respuesta actualizada con la nueva foto
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Actualizamos la pregunta con la nueva respuesta
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Elimina una foto de una pregunta específica.
     *
     * @param questionId ID de la pregunta de la que se elimina la foto
     * @param photo La foto a eliminar
     */
    fun removePhotoFromQuestion(questionId: String, photo: Photo) {
        val question = getQuestionById(questionId) ?: return

        // Obtenemos la respuesta actual
        val currentAnswer = question.answer ?: return

        // Eliminamos la foto de la lista
        val updatedPhotos = currentAnswer.photos.filter { it.id != photo.id }

        // Creamos una respuesta actualizada sin la foto eliminada
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Actualizamos la pregunta con la nueva respuesta
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Actualiza una foto con los dibujos realizados.
     *
     * @param questionId ID de la pregunta que contiene la foto
     * @param photoId ID de la foto a actualizar
     * @param drawingUri URI de la imagen con los dibujos
     */
    fun updatePhotoWithDrawing(questionId: String, photoId: String, drawingUri: String) {
        val question = getQuestionById(questionId) ?: return

        // Obtenemos la respuesta actual
        val currentAnswer = question.answer ?: return

        // Encontramos la foto a actualizar
        val photoIndex = currentAnswer.photos.indexOfFirst { it.id == photoId }
        if (photoIndex == -1) return

        // Obtenemos la foto actual
        val photo = currentAnswer.photos[photoIndex]

        // Creamos una nueva foto con la información del dibujo
        val updatedPhoto = photo.copy(
            hasDrawings = true,
            drawingUri = drawingUri
        )

        // Actualizamos la lista de fotos
        val updatedPhotos = currentAnswer.photos.toMutableList().apply {
            set(photoIndex, updatedPhoto)
        }

        // Creamos una respuesta actualizada con la foto modificada
        val updatedAnswer = currentAnswer.copy(photos = updatedPhotos)

        // Actualizamos la pregunta con la nueva respuesta
        updateQuestionWithAnswer(questionId, updatedAnswer)
    }

    /**
     * Actualiza una pregunta con una nueva respuesta.
     *
     * @param questionId ID de la pregunta a actualizar
     * @param answer La nueva respuesta
     */
    private fun updateQuestionWithAnswer(questionId: String, answer: Answer) {
        // Buscamos el ítem y la pregunta
        val items = inspection.items.toMutableList()

        for (i in items.indices) {
            val item = items[i]
            val questions = item.questions.toMutableList()

            val questionIndex = questions.indexOfFirst { it.id == questionId }
            if (questionIndex != -1) {
                // Actualizamos la pregunta con la nueva respuesta
                questions[questionIndex] = questions[questionIndex].copy(answer = answer)

                // Actualizamos el ítem con las preguntas actualizadas
                items[i] = item.copy(questions = questions)

                // Actualizamos la inspección completa
                inspection = inspection.copy(items = items)
                return
            }
        }
    }

    /**
     * Busca una pregunta por su ID.
     *
     * @param questionId ID de la pregunta a buscar
     * @return La pregunta encontrada o null si no existe
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
     * Retrocede a la etapa anterior en el proceso de inspección.
     */
    fun goBack(): Boolean {
        return when (currentStage) {
            InspectionStage.INITIAL_INFO -> false // No hay paso anterior
            InspectionStage.CHECKLIST -> {
                if (currentItemIndex > 0) {
                    // Retroceder al ítem anterior
                    currentItemIndex--
                    true
                } else {
                    // Volver a la información inicial
                    currentStage = InspectionStage.INITIAL_INFO
                    true
                }
            }
            InspectionStage.SUMMARY -> {
                // Volver al último ítem del checklist
                currentStage = InspectionStage.CHECKLIST
                currentItemIndex = inspection.items.size - 1
                true
            }
            InspectionStage.COMPLETED -> {
                // Volver al resumen
                currentStage = InspectionStage.SUMMARY
                true
            }
        }
    }

    /**
     * Reinicia la inspección a su estado inicial.
     * Útil cuando se quiere comenzar una nueva inspección.
     */
    fun resetInspection() {
        inspection = Inspection()
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
     * Limpia el mensaje de error
     */
    fun clearError() {
        errorMessage = null
    }
}

/**
 * Factory class for creating NewInspectionViewModel with application parameter
 */
class NewInspectionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewInspectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewInspectionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Enumera las diferentes etapas del proceso de inspección.
 */
enum class InspectionStage {
    INITIAL_INFO,   // Ingreso de información inicial (equipo, inspector, supervisor)
    CHECKLIST,      // Completar el checklist de inspección
    SUMMARY,        // Resumen de la inspección realizada
    COMPLETED       // Inspección completada y guardada
}