package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.ui.components.LabeledTextField
import com.tudominio.checklistapp.ui.components.NumericTextField
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.EquipmentType
import com.tudominio.checklistapp.ui.viewmodels.InspectionStage
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel

/**
 * Pantalla principal para crear una nueva inspección.
 * Gestiona todo el flujo desde el ingreso de datos iniciales
 * hasta la finalización de la inspección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionScreen(
    onNavigateBack: () -> Unit,
    onInspectionCompleted: () -> Unit = {},
    viewModel: NewInspectionViewModel = viewModel(),
    onNavigateToCamera: (String) -> Unit = {},
    onNavigateToPhotos: (String) -> Unit = {}
) {
    // Add debug effect
    LaunchedEffect(key1 = true) {
        println("NewInspectionScreen initial composition")
    }

    // Function to handle back navigation
    val handleBackNavigation = {
        val navigatedBack = viewModel.goBack()
        // If we couldn't navigate back in the inspection flow,
        // return to the previous screen
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
                            InspectionStage.INITIAL_INFO -> "Nueva Inspección"
                            InspectionStage.CHECKLIST -> "Checklist de Inspección"
                            InspectionStage.SUMMARY -> "Resumen de Inspección"
                            InspectionStage.COMPLETED -> "Inspección Completada"
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
                    InitialInfoForm(viewModel)
                }
                InspectionStage.CHECKLIST -> {
                    ChecklistScreen(
                        viewModel = viewModel,
                        onProceed = { viewModel.proceedToNextStage() },
                        onNavigateToCamera = onNavigateToCamera,
                        onNavigateToPhotos = onNavigateToPhotos
                    )
                }
                InspectionStage.SUMMARY -> {
                    SummaryScreen(
                        viewModel = viewModel,
                        onFinish = { viewModel.proceedToNextStage() }
                    )
                }
                InspectionStage.COMPLETED -> {
                    CompletedScreen(
                        viewModel = viewModel,
                        onBackToHome = onInspectionCompleted
                    )
                }
            }
        }
    }
}

/**
 * Form for entering initial inspection information:
 * equipment, inspector, supervisor and horometer.
 */
@Composable
fun InitialInfoForm(viewModel: NewInspectionViewModel) {
    var equipmentNumberValue by remember { mutableStateOf(viewModel.equipmentNumber) }
    var isCustomEquipment by remember { mutableStateOf(false) }

    // Validate equipment ID format
    fun validateEquipment(value: String, equipmentType: EquipmentType): Boolean {
        if (value.isBlank()) return false

        try {
            val caexNumber = value.toInt()

            return when (equipmentType) {
                EquipmentType.CAEX_797F -> (caexNumber in 301..339) || caexNumber == 365 || caexNumber == 366
                EquipmentType.CAEX_798AC -> caexNumber in 340..352
            }
        } catch (e: NumberFormatException) {
            return false
        }
    }

    // Verify and update equipment
    fun updateEquipmentValue(value: String) {
        equipmentNumberValue = value
        isCustomEquipment = !validateEquipment(value, viewModel.selectedEquipmentType)
        viewModel.updateEquipmentNumber(value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form title
        Text(
            text = "Información Inicial",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Helper card with instructions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Indicaciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Complete los datos del equipo que está inspeccionando.\n" +
                            "1. Seleccione el tipo de CAEX\n" +
                            "2. Ingrese solo el número de identificación del equipo",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Equipment type selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tipo de Equipo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    EquipmentType.values().forEach { equipmentType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (viewModel.selectedEquipmentType == equipmentType),
                                    onClick = {
                                        viewModel.updateEquipmentType(equipmentType)
                                        // Revalidate equipment number with new type
                                        isCustomEquipment = !validateEquipment(
                                            equipmentNumberValue,
                                            equipmentType
                                        )
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = viewModel.selectedEquipmentType == equipmentType,
                                onClick = null // handled by the row's selectable
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = equipmentType.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = when (equipmentType) {
                                        EquipmentType.CAEX_797F -> "Números de equipo: 301-339, 365, 366"
                                        EquipmentType.CAEX_798AC -> "Números de equipo: 340-352"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Equipment number field
        LabeledTextField(
            value = equipmentNumberValue,
            onValueChange = { updateEquipmentValue(it) },
            label = "Número de Equipo",
            isError = viewModel.equipmentError || isCustomEquipment,
            errorMessage = when {
                viewModel.equipmentError -> "El número de equipo es obligatorio"
                isCustomEquipment -> when (viewModel.selectedEquipmentType) {
                    EquipmentType.CAEX_797F -> "Número inválido. Use 301-339, 365 o 366"
                    EquipmentType.CAEX_798AC -> "Número inválido. Use 340-352"
                }
                else -> ""
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // Inspector field
        LabeledTextField(
            value = viewModel.inspection.inspector,
            onValueChange = { viewModel.updateInspector(it) },
            label = "Inspector",
            isError = viewModel.inspectorError,
            errorMessage = "El inspector es obligatorio",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Supervisor field
        LabeledTextField(
            value = viewModel.inspection.supervisor,
            onValueChange = { viewModel.updateSupervisor(it) },
            label = "Supervisor de Taller",
            isError = viewModel.supervisorError,
            errorMessage = "El supervisor es obligatorio",
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Horometer field
        NumericTextField(
            value = viewModel.inspection.horometer,
            onValueChange = { viewModel.updateHorometer(it) },
            label = "Horómetro",
            isError = viewModel.horometerError,
            errorMessage = "El horómetro es obligatorio",
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Continue button
        PrimaryButton(
            text = "Continuar",
            onClick = { viewModel.proceedToNextStage() },
            enabled = !viewModel.equipmentError && !viewModel.inspectorError &&
                    !viewModel.supervisorError && !viewModel.horometerError &&
                    !isCustomEquipment
        )
    }
}