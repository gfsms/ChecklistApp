package com.tudominio.checklistapp.data.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo que representa una inspección completa.
 * Contiene información sobre el equipo inspeccionado, el personal involucrado,
 * y los resultados de la inspección (preguntas y respuestas).
 */
data class Inspection(
    // Identificador único de la inspección
    val id: String = UUID.randomUUID().toString(),

    // Información básica de la inspección
    val equipment: String = "",
    val inspector: String = "",
    val supervisor: String = "",
    val horometer: String = "",
    val date: LocalDateTime = LocalDateTime.now(),

    // Lista de elementos de inspección (organizados por ítem)
    val items: List<InspectionItem> = emptyList(),

    // Estado de la inspección
    val isCompleted: Boolean = false
)

/**
 * Representa un ítem o sección dentro de la inspección.
 * Un ítem contiene varias preguntas relacionadas.
 */
data class InspectionItem(
    val id: String,
    val name: String,
    val questions: List<InspectionQuestion> = emptyList()
)

/**
 * Representa una pregunta específica dentro de un ítem de inspección.
 */
data class InspectionQuestion(
    val id: String,
    val text: String,
    var answer: Answer? = null
)

/**
 * Respuesta a una pregunta de inspección.
 * Puede ser conforme o no conforme. En caso de ser no conforme,
 * debe incluir un comentario y puede tener fotos asociadas.
 */
data class Answer(
    val isConform: Boolean,
    val comment: String = "",
    val photos: List<Photo> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Representa una foto tomada como evidencia durante la inspección.
 * Puede contener anotaciones o dibujos realizados por el usuario.
 */
data class Photo(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val hasDrawings: Boolean = false,
    val drawingUri: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)