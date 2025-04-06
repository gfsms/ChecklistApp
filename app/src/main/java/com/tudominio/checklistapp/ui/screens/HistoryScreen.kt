// app/src/main/java/com/tudominio/checklistapp/ui/screens/HistoryScreen.kt
package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.ui.components.InspectionInfoSummary
import com.tudominio.checklistapp.ui.components.InspectionStatsSummary
import com.tudominio.checklistapp.ui.components.NonConformitiesSummary
import com.tudominio.checklistapp.ui.viewmodels.HistoryViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val inspections by viewModel.inspections.collectAsState()
    val selectedInspection by viewModel.selectedInspection.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedInspection == null) "Historial de Inspecciones" else "Detalles de Inspección") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedInspection == null) {
                            onNavigateBack()
                        } else {
                            viewModel.clearSelectedInspection()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (selectedInspection != null) {
                // Mostrar detalles de la inspección seleccionada
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        selectedInspection?.let { inspection ->
                            InspectionInfoSummary(inspection = inspection)
                            Spacer(modifier = Modifier.height(16.dp))
                            InspectionStatsSummary(inspection = inspection)
                            Spacer(modifier = Modifier.height(16.dp))
                            NonConformitiesSummary(items = inspection.items)
                        }
                    }
                }
            } else {
                // Mostrar lista de inspecciones
                if (inspections.isEmpty()) {
                    Text(
                        text = "No hay inspecciones guardadas",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(inspections) { inspection ->
                            InspectionListItem(inspection = inspection) {
                                viewModel.loadInspectionDetails(inspection.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionListItem(
    inspection: InspectionEntity,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = inspection.date.format(dateFormatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Equipo: ${inspection.equipment}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fecha: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Inspector: ${inspection.inspector}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de conformidad
            val conformityColor = when {
                inspection.conformityPercentage >= 90f -> Color(0xFF4CAF50) // Verde
                inspection.conformityPercentage >= 70f -> Color(0xFFFFC107) // Amarillo
                else -> Color(0xFFF44336) // Rojo
            }

            Text(
                text = "Conformidad: ${inspection.conformityPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = inspection.conformityPercentage / 100f,  // Cambiar esto
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = conformityColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}