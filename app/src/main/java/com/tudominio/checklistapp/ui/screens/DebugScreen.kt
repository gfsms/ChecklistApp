package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.data.database.InspectionEntity
import com.tudominio.checklistapp.ui.theme.Green
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.ui.theme.Yellow
import com.tudominio.checklistapp.ui.viewmodels.DebugViewModel
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import java.time.format.DateTimeFormatter

/**
 * A debug screen to diagnose database issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onNavigateBack: () -> Unit,
    newInspectionViewModel: NewInspectionViewModel,
    debugViewModel: DebugViewModel = viewModel()
) {
    val snackbarHostState = androidx.compose.material3.SnackbarHostState()

    // Show any errors in the snackbar
    LaunchedEffect(debugViewModel.errorMessage) {
        debugViewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            debugViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico de Base de Datos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { debugViewModel.testDatabaseAccess() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Database connection status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (debugViewModel.databaseAccessSuccessful) {
                        true -> Green.copy(alpha = 0.2f)
                        false -> Red.copy(alpha = 0.2f)
                        null -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (debugViewModel.databaseAccessSuccessful) {
                        true -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Green,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Conexión a la Base de Datos: EXITOSA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        false -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Conexión a la Base de Datos: FALLIDA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        null -> {
                            if (debugViewModel.isLoading) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Probando conexión...",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else {
                                Text(
                                    text = "Estado desconocido",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            // Save current inspection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Guardar Inspección Actual",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Equipo: ${newInspectionViewModel.inspection.equipment}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                debugViewModel.saveCurrentInspection(newInspectionViewModel)
                            },
                            enabled = !debugViewModel.isLoading && newInspectionViewModel.inspection.equipment.isNotBlank()
                        ) {
                            Text("Guardar Solo Datos Básicos")
                        }
                    }
                }
            }

            // Inspections list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Inspecciones en Base de Datos",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (debugViewModel.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (debugViewModel.inspections.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay inspecciones en la base de datos",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(debugViewModel.inspections) { inspection ->
                                DebugInspectionItem(inspection)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DebugInspectionItem(inspection: InspectionEntity) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = inspection.date.format(dateFormatter)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ID: ${inspection.id}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Equipo: ${inspection.equipment}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Inspector: ${inspection.inspector}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Fecha: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val conformityColor = when {
                inspection.conformityPercentage >= 90f -> Green
                inspection.conformityPercentage >= 70f -> Yellow
                else -> Red
            }

            Text(
                text = "Conformidad: ${inspection.conformityPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = conformityColor
            )
        }
    }
}