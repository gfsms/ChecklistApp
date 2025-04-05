package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.ui.components.ChecklistItemHeader
import com.tudominio.checklistapp.ui.components.ChecklistQuestionItem
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel

/**
 * Pantalla que muestra el checklist de una inspección.
 * Muestra las preguntas organizadas por ítems y permite al usuario
 * responder a cada pregunta como conforme o no conforme.
 *
 * @param viewModel ViewModel que gestiona el estado de la inspección
 * @param onProceed Callback para avanzar a la siguiente sección
 * @param onNavigateToCamera Callback para navegar a la pantalla de cámara
 * @param onNavigateToPhotos Callback para navegar a la pantalla de gestión de fotos
 */
@Composable
fun ChecklistScreen(
    viewModel: NewInspectionViewModel,
    onProceed: () -> Unit,
    onNavigateToCamera: (String) -> Unit = {},
    onNavigateToPhotos: (String) -> Unit = {}
) {
    // Estado de carga
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Obtenemos el ítem actual que estamos inspeccionando
    val currentItem = if (viewModel.inspection.items.isNotEmpty() &&
        viewModel.currentItemIndex < viewModel.inspection.items.size) {
        viewModel.inspection.items[viewModel.currentItemIndex]
    } else {
        null
    }

    // Si no hay ítems o el índice es inválido, mostramos un mensaje
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

    // Verificamos si todas las preguntas del ítem actual han sido respondidas
    val allQuestionsAnswered by remember(currentItem) {
        derivedStateOf {
            currentItem.questions.all { it.answer != null }
        }
    }

    // Verificamos si todas las preguntas no conformes tienen comentario
    val allNonConformHaveComments by remember(currentItem) {
        derivedStateOf {
            currentItem.questions.all { question ->
                val answer = question.answer
                answer == null || answer.isConform || (!answer.isConform && answer.comment.isNotBlank())
            }
        }
    }

    // Habilitamos el botón de continuar solo si se cumplen ambas condiciones
    val canProceed = allQuestionsAnswered && allNonConformHaveComments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Contenido principal del checklist (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título del ítem
            ChecklistItemHeader(
                title = currentItem.name,
                number = (viewModel.currentItemIndex + 1).toString()
            )

            // Información sobre el progreso
            Text(
                text = "Ítem ${viewModel.currentItemIndex + 1} de ${viewModel.inspection.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Mostramos cada pregunta del ítem actual
            currentItem.questions.forEachIndexed { index, question ->
                ChecklistQuestionItem(
                    question = question,
                    onAnswerChange = { q, answer ->
                        viewModel.updateQuestionAnswer(q, answer)
                    },
                    onAddPhoto = { q ->
                        // Navegamos a la pantalla de cámara con el ID de la pregunta
                        onNavigateToCamera(q.id)
                    },
                    onViewPhotos = { q ->
                        // Navegamos a la pantalla de fotos con el ID de la pregunta
                        onNavigateToPhotos(q.id)
                    }
                )

                if (index < currentItem.questions.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Sección de mensajes y botón de continuar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Mensajes de validación
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
            } else if (!allNonConformHaveComments) {
                Text(
                    text = "Todas las preguntas no conformes deben tener un comentario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Botón para continuar
            PrimaryButton(
                text = if (viewModel.currentItemIndex < viewModel.inspection.items.size - 1)
                    "Siguiente Ítem" else "Finalizar Inspección",
                onClick = onProceed,
                enabled = canProceed
            )
        }
    }
}