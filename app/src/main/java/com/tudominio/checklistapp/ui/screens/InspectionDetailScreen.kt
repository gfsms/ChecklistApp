package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.ui.components.ExportComponent
import com.tudominio.checklistapp.ui.components.InspectionInfoSummary
import com.tudominio.checklistapp.ui.components.InspectionStatsSummary
import com.tudominio.checklistapp.ui.components.NonConformitiesSummary
import com.tudominio.checklistapp.ui.viewmodels.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    inspectionId: String,
    onNavigateBack: () -> Unit,
    onExport: (Inspection) -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    // Load the inspection details
    LaunchedEffect(inspectionId) {
        viewModel.loadInspectionDetails(inspectionId)
    }

    // Get the selected inspection
    val selectedInspection = viewModel.selectedInspection
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Inspección") },
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
        },
        floatingActionButton = {
            selectedInspection?.let { inspection ->
                FloatingActionButton(
                    onClick = { onExport(inspection) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Exportar"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                selectedInspection == null -> {
                    Text(
                        text = "No se encontró la inspección",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    // Display inspection details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Basic information summary
                        InspectionInfoSummary(
                            inspection = selectedInspection,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Statistics summary
                        InspectionStatsSummary(
                            inspection = selectedInspection,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )

                        // Non-conformities summary
                        NonConformitiesSummary(
                            items = selectedInspection.items,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )

                        // Export component
                        ExportComponent(
                            inspection = selectedInspection,
                            onExportStarted = { /* Handle export started */ },
                            onExportCompleted = { /* Handle export completed */ }
                        )
                    }
                }
            }
        }
    }
}