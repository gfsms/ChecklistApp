package com.tudominio.checklistapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow
import java.time.format.DateTimeFormatter

/**
 * Componente que muestra un resumen de la información básica de la inspección.
 *
 * @param inspection La inspección a mostrar
 * @param modifier Modificador opcional
 */
@Composable
fun InspectionInfoSummary(
    inspection: Inspection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información de Inspección",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Fecha y hora de la inspección
            val formattedDate = inspection.date.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            )

            InfoRow(label = "Fecha", value = formattedDate)
            InfoRow(label = "Equipo", value = inspection.equipment)
            InfoRow(label = "Inspector", value = inspection.inspector)
            InfoRow(label = "Supervisor", value = inspection.supervisor)
            InfoRow(label = "Horómetro", value = inspection.horometer)
        }
    }
}

/**
 * Componente que muestra una fila con etiqueta y valor para la información de resumen.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Componente que muestra un resumen de las no conformidades encontradas.
 *
 * @param items Lista de ítems de la inspección
 * @param modifier Modificador opcional
 */
@Composable
fun NonConformitiesSummary(
    items: List<InspectionItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título con ícono de advertencia
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Yellow,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "No Conformidades",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Contamos no conformidades
            val nonConformCount = items.sumOf { item ->
                item.questions.count { question ->
                    question.answer != null && !question.answer!!.isConform
                }
            }

            if (nonConformCount == 0) {
                Text(
                    text = "No se encontraron no conformidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green
                )
            } else {
                // Mostramos las no conformidades agrupadas por ítem
                items.forEachIndexed { index, item ->
                    val nonConformQuestions = item.questions.filter { question ->
                        val answer = question.answer
                        answer != null && !answer.isConform
                    }

                    if (nonConformQuestions.isNotEmpty()) {
                        ItemNonConformities(
                            itemName = item.name,
                            itemIndex = index + 1,
                            questions = nonConformQuestions
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra las no conformidades de un ítem específico.
 */
@Composable
fun ItemNonConformities(
    itemName: String,
    itemIndex: Int,
    questions: List<InspectionQuestion>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título del ítem
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con número
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemIndex.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = itemName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Preguntas no conformes
        questions.forEach { question ->
            NonConformityItem(question = question)
        }
    }
}

/**
 * Componente que muestra una pregunta no conforme con su comentario.
 */
@Composable
fun NonConformityItem(
    question: InspectionQuestion,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pregunta
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Indicador de no conforme
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Red)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Comentario
        val comment = question.answer?.comment ?: ""
        Text(
            text = comment,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Componente que muestra un resumen estadístico de la inspección.
 */
@Composable
fun InspectionStatsSummary(
    inspection: Inspection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Cálculo de estadísticas
            val totalQuestions = inspection.items.sumOf { it.questions.size }
            val answeredQuestions = inspection.items.sumOf { item ->
                item.questions.count { it.answer != null }
            }
            val conformQuestions = inspection.items.sumOf { item ->
                item.questions.count { question ->
                    val answer = question.answer
                    answer != null && answer.isConform
                }
            }
            val nonConformQuestions = inspection.items.sumOf { item ->
                item.questions.count { question ->
                    val answer = question.answer
                    answer != null && !answer.isConform
                }
            }

            // Porcentaje de conformidad
            val conformityPercentage = if (answeredQuestions > 0) {
                (conformQuestions.toFloat() / answeredQuestions) * 100
            } else {
                0f
            }

            // Mostrar estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    value = totalQuestions.toString(),
                    label = "Preguntas Totales",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatCard(
                    value = nonConformQuestions.toString(),
                    label = "No Conformidades",
                    valueColor = if (nonConformQuestions > 0) Red else Color.Unspecified,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatCard(
                value = "%.1f%%".format(conformityPercentage),
                label = "Conformidad",
                valueColor = when {
                    conformityPercentage >= 90f -> Green
                    conformityPercentage >= 70f -> Yellow
                    else -> Red
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Tarjeta que muestra un valor estadístico con una etiqueta.
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    valueColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor.takeIf { it != Color.Unspecified }
                    ?: MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}