package com.tudominio.checklistapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow

// Data models for analytics
data class EquipmentStats(
    val equipmentId: String,
    val inspectionCount: Int,
    val averageConformity: Float,
    val trend: Float
)

data class CommonIssue(
    val itemName: String,
    val questionText: String,
    val occurrenceCount: Int,
    val equipments: List<String>
)

@Composable
fun AnalyticsDashboard(modifier: Modifier = Modifier) {
    // Mock data for demonstration
    val mockEquipmentStats = remember {
        listOf(
            EquipmentStats("CAEX 301", 12, 87.5f, 5.2f),
            EquipmentStats("CAEX 302", 8, 75.3f, -2.1f),
            EquipmentStats("CAEX 303", 10, 92.1f, 3.7f),
            EquipmentStats("CAEX 304", 7, 68.9f, -4.5f),
            EquipmentStats("CAEX 305", 9, 81.4f, 0.8f)
        )
    }

    val mockCommonIssues = remember {
        listOf(
            CommonIssue(
                "Sistema Hidráulico",
                "¿Hay fugas de aceite en mangueras o conexiones?",
                7,
                listOf("CAEX 301", "CAEX 302", "CAEX 304")
            ),
            CommonIssue(
                "Sistema Eléctrico",
                "¿El cableado presenta deterioro o daños?",
                5,
                listOf("CAEX 302", "CAEX 304")
            ),
            CommonIssue(
                "Sistema de Frenos",
                "¿Los discos de freno presentan desgaste excesivo?",
                4,
                listOf("CAEX 301", "CAEX 305")
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Analítica de Inspecciones",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Summary cards
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryCard(
                    title = "Inspecciones Totales",
                    value = "46",
                    subtitle = "+12% este mes",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                SummaryCard(
                    title = "Conformidad Promedio",
                    value = "81%",
                    subtitle = "+3% desde último mes",
                    color = Green
                )
            }

            item {
                SummaryCard(
                    title = "No Conformidades",
                    value = "23",
                    subtitle = "-5% desde último mes",
                    color = Red
                )
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        // Equipment performance
        Text(
            text = "Rendimiento por Equipo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockEquipmentStats) { stats ->
                EquipmentCard(stats)
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        // Common issues
        Text(
            text = "Problemas Comunes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mockCommonIssues.forEach { issue ->
                IssueCard(issue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = "Nota: Los datos mostrados son ejemplos. La analítica completa estará disponible en futuras versiones.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

@Composable
fun EquipmentCard(stats: EquipmentStats) {
    val conformityColor = when {
        stats.averageConformity >= 90f -> Green
        stats.averageConformity >= 70f -> Yellow
        else -> Red
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stats.equipmentId,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column {
                Text(
                    text = "Inspecciones",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = stats.inspectionCount.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column {
                Text(
                    text = "Conformidad Promedio",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.averageConformity.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = conformityColor
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    if (stats.trend != 0f) {
                        val trendIcon = if (stats.trend > 0)
                            Icons.Default.ArrowUpward else Icons.Default.Warning
                        val trendColor = if (stats.trend > 0) Green else Red

                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = "${kotlin.math.abs(stats.trend)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = trendColor
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = stats.averageConformity / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(top = 4.dp),
                    color = conformityColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun IssueCard(issue: CommonIssue) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = issue.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = issue.itemName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ocurrencias",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = issue.occurrenceCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (issue.occurrenceCount > 5) Red else Yellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Equipos afectados: ${issue.equipments.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}