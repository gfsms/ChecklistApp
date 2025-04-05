package com.tudominio.checklistapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion

/**
 * ViewModel que gestiona el estado y la lógica de la pantalla de Nueva Inspección.
 * Se encarga de mantener el estado del formulario, validar los campos y
 * gestionar la navegación entre las diferentes etapas del proceso de inspección.
 */
class NewInspectionViewModel : ViewModel() {

    // Estado de la inspección actual
    var inspection by mutableStateOf(Inspection())
        private set

    // Estado de la pantalla
    var isLoading by mutableStateOf(false)
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
                    // En una implementación real, aquí cargaríamos los ítems del checklist
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
     * Carga los ítems del checklist para la inspección.
     * En una aplicación real, estos datos podrían venir de una base de datos o API.
     */
    private fun loadChecklistItems() {
        // Simulamos la carga de datos
        isLoading = true

        // Por ahora, creamos unos ítems de ejemplo
        val items = listOf(
            InspectionItem(
                id = "item1",
                name = "Item 1",
                questions = listOf(
                    InspectionQuestion(id = "item1_q1", text = "Pregunta 1"),
                    InspectionQuestion(id = "item1_q2", text = "Pregunta 2"),
                    InspectionQuestion(id = "item1_q3", text = "Pregunta 3")
                )
            ),
            InspectionItem(
                id = "item2",
                name = "Item 2",
                questions = listOf(
                    InspectionQuestion(id = "item2_q1", text = "Pregunta 1"),
                    InspectionQuestion(id = "item2_q2", text = "Pregunta 2"),
                    InspectionQuestion(id = "item2_q3", text = "Pregunta 3")
                )
            )
        )

        inspection = inspection.copy(items = items)
        isLoading = false
    }

    /**
     * Guarda la inspección completada.
     * En una aplicación real, esto podría guardar en una base de datos local o enviar a un servidor.
     */
    private fun saveInspection() {
        isLoading = true
        // Aquí implementaríamos la lógica de guardado
        inspection = inspection.copy(isCompleted = true)
        isLoading = false
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
            // Actualizamos la pregunta con la nueva respuesta
            questions[questionIndex] = questions[questionIndex].copy(answer = answer)

            // Creamos un nuevo ítem con las preguntas actualizadas
            val updatedItem = currentItem.copy(questions = questions)

            // Actualizamos la lista de ítems
            currentItems[currentItemIndex] = updatedItem

            // Actualizamos la inspección completa
            inspection = inspection.copy(items = currentItems)
        }
    }

    /**
     * Verifica si todas las preguntas del ítem actual han sido respondidas.
     *
     * @return true si todas las preguntas tienen respuesta, false en caso contrario
     */
    fun areAllQuestionsAnswered(): Boolean {
        if (inspection.items.isEmpty() || currentItemIndex >= inspection.items.size) {
            return false
        }

        val currentItem = inspection.items[currentItemIndex]
        return currentItem.questions.all { it.answer != null }
    }

    /**
     * Verifica si todas las preguntas no conformes tienen comentarios.
     *
     * @return true si todas las preguntas no conformes tienen comentarios, false en caso contrario
     */
    fun doAllNonConformHaveComments(): Boolean {
        if (inspection.items.isEmpty() || currentItemIndex >= inspection.items.size) {
            return false
        }

        val currentItem = inspection.items[currentItemIndex]
        return currentItem.questions.all { question ->
            val answer = question.answer
            answer == null || answer.isConform || (!answer.isConform && answer.comment.isNotBlank())
        }
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