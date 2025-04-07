package com.tudominio.checklistapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.ui.theme.Yellow

/**
 * Dialog component to display recurring non-conformity warnings
 */
@Composable
fun RecurringNonConformityDialog(
    inspection: Inspection,
    onDismiss: () -> Unit
) {
    // Create warning messages for any non-conformities
    val messages = mutableListOf<String>()

    inspection.items.forEach { item ->
        item.questions.forEach { question ->
            if (question.answer?.isConform == false) {
                // In a real implementation, this would check the database
                // Since we can't access the database, we'll add a mock warning for each non-conformity
                messages.add("La pregunta '${question.text}' en el ítem '${item.name}' puede ser un problema recurrente en el equipo ${inspection.equipment}.")
            }
        }
    }

    // Only show dialog if there are messages
    if (messages.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Yellow
                )
            },
            title = { Text("Hallazgos Potencialmente Recurrentes") },
            text = {
                Column {
                    Text(
                        text = "Se encontraron no conformidades que podrían ser recurrentes:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(messages) { message ->
                            Text(
                                text = "• $message",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Entendido")
                }
            }
        )
    }
}