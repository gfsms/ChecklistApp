package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.ui.components.NumericTextField
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.InspectionStage
import com.tudominio.checklistapp.ui.viewmodels.PostInspectionViewModel

/**
 * Main screen for post-intervention inspection process
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostInspectionScreen(
    controlInspectionId: String,
    onNavigateBack: () -> Unit,
    onInspectionCompleted: () -> Unit = {},
    onNavigateToCamera: (String) -> Unit = {},
    onNavigateToPhotos: (String) -> Unit = {},
    viewModel: PostInspectionViewModel = viewModel()
) {
    // Initialize post-inspection from control inspection
    LaunchedEffect(controlInspectionId) {
        viewModel.initializePostInspection(controlInspectionId)
    }

    // Function to handle back navigation
    val handleBackNavigation = {
        val navigatedBack = viewModel.goBack()
        // If we couldn't navigate back in the flow, return to previous screen
        if (!navigatedBack) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (viewModel.currentStage) {
                            InspectionStage.INITIAL_INFO -> "Entrega de Equipo"
                            InspectionStage.CHECKLIST -> "Checklist de Entrega"
                            InspectionStage.SUMMARY -> "Resumen de Entrega"
                            InspectionStage.COMPLETED -> "Entrega Completada"
                            else -> "Inspección de Entrega" // Added else branch for exhaustiveness
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackNavigation) {
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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            // Show the current stage of the inspection process
            when (viewModel.currentStage) {
                InspectionStage.INITIAL_INFO -> {
                    PostInspectionInfoForm(viewModel)
                }
                InspectionStage.CHECKLIST -> {
                    PostInspectionChecklistScreen(
                        viewModel = viewModel,
                        onProceed = { viewModel.proceedToNextStage() },
                        onNavigateToCamera = onNavigateToCamera,
                        onNavigateToPhotos = onNavigateToPhotos
                    )
                }
                InspectionStage.SUMMARY -> {
                    // Use a modified SummaryScreen for post-inspection
                    PostInspectionSummaryScreen(
                        viewModel = viewModel,
                        onFinish = { viewModel.proceedToNextStage() }
                    )
                }
                InspectionStage.COMPLETED -> {
                    PostInspectionCompletedScreen(
                        viewModel = viewModel,
                        onBackToHome = {
                            viewModel.reset()
                            onInspectionCompleted()
                        }
                    )
                }
                else -> {
                    // Fallback for exhaustiveness
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Estado de inspección desconocido")
                    }
                }
            }
        }
    }
}

/**
 * Initial form for post-intervention inspection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostInspectionInfoForm(viewModel: PostInspectionViewModel) {
    val equipment = viewModel.postInspection.equipment

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form title
        Text(
            text = "Entrega de Equipo Post Intervención",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Equipment ID (non-editable)
        TextField(
            value = equipment,
            onValueChange = { /* Read-only */ },
            label = { Text("Equipo") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Inspector field
        TextField(
            value = viewModel.postInspection.inspector,
            onValueChange = { viewModel.updateInspector(it) },
            label = { Text("Inspector") },
            isError = viewModel.postInspection.inspector.isBlank(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Supervisor field
        TextField(
            value = viewModel.postInspection.supervisor,
            onValueChange = { viewModel.updateSupervisor(it) },
            label = { Text("Supervisor de Taller") },
            isError = viewModel.postInspection.supervisor.isBlank(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Horometer field
        NumericTextField(
            value = viewModel.postInspection.horometer,
            onValueChange = { viewModel.updateHorometer(it) },
            label = "Horómetro",
            isError = viewModel.postInspection.horometer.isBlank(),
            errorMessage = "El horómetro es obligatorio"
        )

        // Continue button
        PrimaryButton(
            text = "Continuar",
            onClick = { viewModel.proceedToNextStage() },
            enabled = viewModel.validateInitialFields()
        )
    }
}

/**
 * Temporary placeholder for post-inspection summary
 */
@Composable
fun PostInspectionSummaryScreen(
    viewModel: PostInspectionViewModel,
    onFinish: () -> Unit
) {
    // For now, we'll reuse the existing SummaryScreen component but pass our view model
    // This is a placeholder - you'll need to create a proper post-inspection summary later
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Resumen de Entrega de Equipo",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Equipo: ${viewModel.postInspection.equipment}",
            style = MaterialTheme.typography.bodyLarge
        )

        PrimaryButton(
            text = "Finalizar Inspección de Entrega",
            onClick = onFinish
        )
    }
}

/**
 * Temporary placeholder for post-inspection completed screen
 */
@Composable
fun PostInspectionCompletedScreen(
    viewModel: PostInspectionViewModel,
    onBackToHome: () -> Unit
) {
    // For now, just show a simple completion message
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Inspección de Entrega Completada!",
            style = MaterialTheme.typography.titleLarge
        )

        PrimaryButton(
            text = "Volver al Inicio",
            onClick = onBackToHome,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}