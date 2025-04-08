package com.tudominio.checklistapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.ui.components.ChecklistItemHeader
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.theme.Yellow
import com.tudominio.checklistapp.ui.viewmodels.FindingType
import com.tudominio.checklistapp.ui.viewmodels.PostInspectionViewModel

/**
 * Checklist screen for post-intervention inspection
 */
@Composable
fun PostInspectionChecklistScreen(
    viewModel: PostInspectionViewModel,
    onProceed: () -> Unit,
    onNavigateToCamera: (String) -> Unit = {},
    onNavigateToPhotos: (String) -> Unit = {}
) {
    // Loading state
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Get current item being inspected
    val currentItem = if (viewModel.postInspection.items.isNotEmpty() &&
        viewModel.currentItemIndex < viewModel.postInspection.items.size) {
        viewModel.postInspection.items[viewModel.currentItemIndex]
    } else {
        null
    }

    // If no items or invalid index, show message
    if (currentItem == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay ítems para inspeccionar",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    // Check if all questions in current item have been answered
    val allQuestionsAnswered by remember(currentItem) {
        derivedStateOf {
            currentItem.questions.all { it.answer != null }
        }
    }

    // Check if all rejected questions have comments
    val allRejectedHaveComments by remember(currentItem) {
        derivedStateOf {
            currentItem.questions.all { question ->
                val answer = question.answer
                answer == null || answer.isConform || (!answer.isConform && answer.comment.isNotBlank())
            }
        }
    }

    // Enable continue button if all conditions are met
    val canProceed = allQuestionsAnswered && allRejectedHaveComments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main checklist content (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item title
            ChecklistItemHeader(
                title = currentItem.name,
                number = (viewModel.currentItemIndex + 1).toString()
            )

            // Progress information
            Text(
                text = "Ítem ${viewModel.currentItemIndex + 1} de ${viewModel.postInspection.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Display each question in current item
            currentItem.questions.forEachIndexed { index, question ->
                PostInspectionQuestionItem(
                    question = question,
                    wasControlFinding = viewModel.wasControlFinding(question.id),
                    findingType = viewModel.getFindingType(question.id),
                    onAnswerChange = { q, answer ->
                        viewModel.updateQuestionAnswer(q, answer)
                    },
                    onAddPhoto = { q ->
                        onNavigateToCamera(q.id)
                    },
                    onViewPhotos = { q ->
                        onNavigateToPhotos(q.id)
                    }
                )

                if (index < currentItem.questions.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Messages and continue button section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Validation messages
            if (!allQuestionsAnswered) {
                Text(
                    text = "Debe responder todas las preguntas para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            } else if (!allRejectedHaveComments) {
                Text(
                    text = "Todas las preguntas rechazadas deben tener un comentario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Continue button
            PrimaryButton(
                text = if (viewModel.currentItemIndex < viewModel.postInspection.items.size - 1)
                    "Siguiente Ítem" else "Finalizar Inspección",
                onClick = onProceed,
                enabled = canProceed
            )
        }
    }
}

/**
 * Special version of question item for post-intervention inspection
 * Highlights questions with control findings and changes accept/reject terminology
 */
@Composable
fun PostInspectionQuestionItem(
    question: InspectionQuestion,
    wasControlFinding: Boolean,
    findingType: FindingType?,
    onAnswerChange: (InspectionQuestion, Answer) -> Unit,
    onAddPhoto: (InspectionQuestion) -> Unit = {},
    onViewPhotos: (InspectionQuestion) -> Unit = {}
) {
    var showCommentSection by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }

    // Check if question is already answered
    val isAnswered = question.answer != null
    // If already answered, update local state
    if (isAnswered && question.answer?.isConform == false) {
        showCommentSection = true
        comment = question.answer?.comment ?: ""
    }

    // Check if question has photos
    val hasPhotos = question.answer?.photos?.isNotEmpty() == true
    val photoCount = question.answer?.photos?.size ?: 0

    // Background color based on control finding status
    val backgroundColor = when {
        wasControlFinding -> Yellow.copy(alpha = 0.15f)
        isAnswered -> if (question.answer?.isConform == true) {
            Color(0xFFE8F5E9) // Light green for accepted
        } else {
            Color(0xFFFFEBEE) // Light red for rejected
        }
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (wasControlFinding) {
                    Modifier.border(
                        width = 2.dp,
                        color = Yellow,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Control finding indicator
            if (wasControlFinding) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Yellow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Yellow,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Hallazgo en inspección de control",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Question text
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Accepted and Rejected buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accepted button
                Button(
                    onClick = {
                        showCommentSection = false
                        onAnswerChange(question, Answer(isConform = true))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswered && question.answer?.isConform == true)
                            Color(0xFF43A047) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isAnswered && question.answer?.isConform == true)
                            Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Aceptado")
                }

                // Rejected button
                Button(
                    onClick = {
                        showCommentSection = true
                        // Keep existing comment if any
                        val existingComment = question.answer?.comment ?: ""
                        comment = existingComment

                        // Keep existing photos if any
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
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswered && question.answer?.isConform == false)
                            Color(0xFFE53935) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isAnswered && question.answer?.isConform == false)
                            Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Rechazado")
                }
            }

            // Comment and photo section for rejected items
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

                    // Comment field
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { newComment ->
                            comment = newComment
                            // Keep existing photos if any
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
                        label = { Text("Justificación (obligatoria)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Finding type indicator for post-intervention findings
                    if (findingType == FindingType.POST_INTERVENTION) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Nuevo hallazgo post-intervención",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Photo buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Add photo button
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

                        // View photos button (only if has photos)
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