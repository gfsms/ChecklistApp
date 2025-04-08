package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow
import com.tudominio.checklistapp.ui.viewmodels.PostInspectionViewModel
import java.time.format.DateTimeFormatter

/**
 * Screen for selecting equipment with control inspections
 * to perform post-intervention inspection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSelectionScreen(
    onNavigateBack: () -> Unit,
    onSelectEquipment: (String) -> Unit,
    viewModel: PostInspectionViewModel = viewModel()
) {
    // Load control inspections when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadControlInspections()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Equipo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar por equipo o inspector") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true
            )

            // Results count and information
            Text(
                text = "${viewModel.filteredInspections.size} equipos con inspección de control",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Information card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Seleccione un equipo para realizar la inspección de entrega",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Se mostrarán los hallazgos encontrados en la inspección de control",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Loading indicator
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Empty state
            else if (viewModel.filteredInspections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No hay equipos con inspección de control",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Primero realice una inspección de Control de Inicio de Intervención",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // List of equipment with control inspections
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.filteredInspections) { inspection ->
                        EquipmentCard(
                            inspection = inspection,
                            onSelect = { onSelectEquipment(inspection.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentCard(
    inspection: Inspection,
    onSelect: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = inspection.date.format(dateFormatter)

    // Calculate non-conformities
    val totalQuestions = inspection.items.sumOf { it.questions.size }
    val nonConformities = inspection.items.sumOf { item ->
        item.questions.count { question ->
            question.answer?.isConform == false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = inspection.equipment,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Inspector: ${inspection.inspector}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Fecha: $formattedDate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Show non-conformities count with color indication
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Hallazgos",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "$nonConformities",
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            nonConformities == 0 -> Green
                            nonConformities <= 3 -> Yellow
                            else -> Red
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}