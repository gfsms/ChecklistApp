package com.tudominio.checklistapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.data.model.Photo
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red

/**
 * Componente que muestra una pregunta del checklist y permite al usuario
 * responderla como conforme o no conforme.
 *
 * @param question La pregunta a mostrar
 * @param onAnswerChange Callback que se invoca cuando cambia la respuesta
 * @param onAddPhoto Callback para abrir la pantalla de captura de foto
 * @param onViewPhotos Callback para ver las fotos existentes
 * @param modifier Modificador opcional para personalizar el componente
 */
@Composable
fun ChecklistQuestionItem(
    question: InspectionQuestion,
    onAnswerChange: (InspectionQuestion, Answer) -> Unit,
    onAddPhoto: (InspectionQuestion) -> Unit = {},
    onViewPhotos: (InspectionQuestion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCommentSection by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }

    // Determinamos si la pregunta ya ha sido respondida
    val isAnswered = question.answer != null
    // Si ya hay respuesta, actualizamos el estado local
    if (isAnswered && question.answer?.isConform == false) {
        showCommentSection = true
        comment = question.answer?.comment ?: ""
    }

    // Comprobamos si la pregunta tiene fotos
    val hasPhotos = question.answer?.photos?.isNotEmpty() == true
    val photoCount = question.answer?.photos?.size ?: 0

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAnswered) {
                if (question.answer?.isConform == true) {
                    Color(0xFFE8F5E9) // Verde muy claro para conforme
                } else {
                    Color(0xFFFFEBEE) // Rojo muy claro para no conforme
                }
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Texto de la pregunta
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Conforme y No Conforme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Conforme
                ConformanceButton(
                    text = "Conforme",
                    isSelected = isAnswered && question.answer?.isConform == true,
                    icon = null,
                    color = Green,
                    onClick = {
                        showCommentSection = false
                        onAnswerChange(question, Answer(isConform = true))
                    },
                    modifier = Modifier.weight(1f)
                )

                // Botón No Conforme
                ConformanceButton(
                    text = "No Conforme",
                    isSelected = isAnswered && question.answer?.isConform == false,
                    icon = null,
                    color = Red,
                    onClick = {
                        showCommentSection = true
                        // Si ya hay un comentario existente, lo mantenemos
                        val existingComment = question.answer?.comment ?: ""
                        comment = existingComment

                        // Mantenemos las fotos existentes si las hay
                        val existingPhotos = question.answer?.photos ?: emptyList()
                        onAnswerChange(
                            question,
                            Answer(
                                isConform = false,
                                comment = existingComment,
                                photos = existingPhotos
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Sección de comentario y foto para No Conforme
            AnimatedVisibility(
                visible = showCommentSection,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo para el comentario
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { newComment ->
                            comment = newComment
                            // Mantenemos las fotos existentes si las hay
                            val existingPhotos = question.answer?.photos ?: emptyList()
                            onAnswerChange(
                                question,
                                Answer(
                                    isConform = false,
                                    comment = newComment,
                                    photos = existingPhotos
                                )
                            )
                        },
                        label = { Text("Comentario (obligatorio)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones para gestionar fotos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón para tomar foto
                        Button(
                            onClick = { onAddPhoto(question) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar Foto",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Agregar Foto")
                        }

                        // Botón para ver fotos (solo si hay fotos)
                        if (hasPhotos) {
                            Button(
                                onClick = { onViewPhotos(question) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Ver Fotos",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Ver Fotos ($photoCount)")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón para indicar conformidad o no conformidad con un diseño personalizado.
 *
 * @param text Texto del botón
 * @param isSelected Si el botón está seleccionado
 * @param icon Icono opcional para el botón
 * @param color Color del botón
 * @param onClick Acción a ejecutar al hacer clic
 * @param modifier Modificador opcional
 */
@Composable
fun ConformanceButton(
    text: String,
    isSelected: Boolean,
    icon: ImageVector?,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                shape = shape
            ),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Encabezado para mostrar el título de un ítem del checklist.
 *
 * @param title Título del ítem
 * @param number Número o identificador del ítem
 * @param modifier Modificador opcional
 */
@Composable
fun ChecklistItemHeader(
    title: String,
    number: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo con el número
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        // Texto del título
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}