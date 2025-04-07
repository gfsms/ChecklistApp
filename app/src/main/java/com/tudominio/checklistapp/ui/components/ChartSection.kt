package com.tudominio.checklistapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow

data class PieChartData(
    val slices: List<PieChartSlice>
)

data class PieChartSlice(
    val value: Float,
    val color: Color,
    val label: String
)

data class BarChartData(
    val bars: List<BarChartBar>
)

data class BarChartBar(
    val value: Float,
    val label: String,
    val color: Color
)

@Composable
fun ChartSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Análisis Visual de Datos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Distribution of conformity pie chart
        val pieChartData = remember {
            PieChartData(
                slices = listOf(
                    PieChartSlice(
                        value = 65f,
                        color = Green,
                        label = "Conforme"
                    ),
                    PieChartSlice(
                        value = 25f,
                        color = Yellow,
                        label = "No Conforme"
                    ),
                    PieChartSlice(
                        value = 10f,
                        color = Red,
                        label = "Sin Revisar"
                    )
                )
            )
        }

        PieChart(
            data = pieChartData,
            title = "Distribución de Conformidad",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Equipment performance bar chart
        val barChartData = remember {
            BarChartData(
                bars = listOf(
                    BarChartBar(
                        value = 92f,
                        label = "CAEX 301",
                        color = Green
                    ),
                    BarChartBar(
                        value = 85f,
                        label = "CAEX 302",
                        color = Green
                    ),
                    BarChartBar(
                        value = 78f,
                        label = "CAEX 303",
                        color = Yellow
                    ),
                    BarChartBar(
                        value = 65f,
                        label = "CAEX 304",
                        color = Yellow
                    ),
                    BarChartBar(
                        value = 45f,
                        label = "CAEX 305",
                        color = Red
                    )
                )
            )
        }

        BarChart(
            data = barChartData,
            title = "Rendimiento por Equipo",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
fun PieChart(
    data: PieChartData,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Create pie chart
                SimplePieChart(
                    data = data,
                    modifier = Modifier.fillMaxSize()
                )

                // Legend
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(start = 32.dp, end = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    data.slices.forEach { slice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(slice.color, RoundedCornerShape(4.dp))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${slice.label} (${slice.value.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimplePieChart(
    data: PieChartData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val total = data.slices.sumOf { it.value.toDouble() }.toFloat()
        val radius = size.minDimension / 3
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f // Start from the top

        data.slices.forEach { slice ->
            val sweepAngle = 360f * (slice.value / total)

            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChart(
    data: BarChartData,
    title: String,
    modifier: Modifier = Modifier
) {
    // Get the outline color before entering Canvas
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Create the bar chart
                SimpleBarChart(
                    data = data,
                    axisColor = outlineColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Add labels separately
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Empty space for the chart
                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),  // Provide space for x-axis
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        data.bars.forEach { bar ->
                            Text(
                                text = bar.label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Add value labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    data.bars.forEach { bar ->
                        // Calculate position to align with bars
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Dynamic positioning based on bar height
                            val barHeightFraction = bar.value / 100f
                            val baseHeight = 0.8f // Base height for the chart area
                            val verticalPosition = baseHeight - (barHeightFraction * baseHeight)

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(verticalPosition)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "${bar.value.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = bar.color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: BarChartData,
    axisColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val maxValue = 100f
        val barWidth = size.width / (data.bars.size * 2)
        val barSpacing = barWidth / 2
        val chartHeight = size.height - 30.dp.toPx()
        val bottomY = chartHeight

        // Draw axes
        drawLine(
            color = axisColor,
            start = Offset(barSpacing, 0f),
            end = Offset(barSpacing, bottomY)
        )

        drawLine(
            color = axisColor,
            start = Offset(barSpacing, bottomY),
            end = Offset(size.width, bottomY)
        )

        // Draw bars
        data.bars.forEachIndexed { index, bar ->
            val barHeight = (bar.value / maxValue) * chartHeight
            val startX = barSpacing + (index * (barWidth + barSpacing))
            val startY = bottomY - barHeight

            drawRect(
                color = bar.color,
                topLeft = Offset(startX, startY),
                size = Size(barWidth, barHeight)
            )
        }
    }
}