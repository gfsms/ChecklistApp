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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudominio.checklistapp.ui.components.LabeledTextField
import com.tudominio.checklistapp.ui.components.NumericTextField
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.InspectionStage
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel

/**
 * Pantalla principal para crear una nueva inspección.
 * Gestiona todo el flujo desde el ingreso de datos iniciales
 * hasta la finalización de la inspección.
 *
 * @param onNavigateBack Función para navegar hacia atrás
 * @param onInspectionCompleted Función que se llama cuando se completa la inspección
 * @param viewModel ViewModel compartido para esta pantalla
 * @param onNavigateToCamera Función para navegar a la pantalla de cámara
 * @param onNavigateToPhotos Función para navegar a la pantalla de gestión de fotos
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
    // Agregamos un efecto para debug
    LaunchedEffect(key1 = true) {
        println("NewInspectionScreen composición inicial")
    }

    // Función para manejar la navegación hacia atrás
    val handleBackNavigation = {
        val navigatedBack = viewModel.goBack()
        // Si no pudimos navegar hacia atrás en el flujo de inspección,
        // volvemos a la pantalla anterior
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
            // Mostramos la etapa actual del proceso de inspección
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
 * Formulario para ingresar la información inicial de la inspección:
 * equipo, inspector, supervisor y horómetro.
 */
@Composable
fun InitialInfoForm(viewModel: NewInspectionViewModel) {
    var equipmentValue by remember { mutableStateOf(viewModel.inspection.equipment) }
    var isCustomEquipment by remember { mutableStateOf(false) }

    // Validate CAEX ID format
    fun validateEquipment(value: String): Boolean {
        if (value.isBlank()) return false

        val caexPattern = "^CAEX (\\d{3})$".toRegex()
        val match = caexPattern.find(value)

        if (match != null) {
            val caexNumber = match.groupValues[1].toInt()
            return (caexNumber in 301..339) || caexNumber == 365 || caexNumber == 366
        }

        return false
    }

    // Verify and update equipment
    fun updateEquipmentValue(value: String) {
        equipmentValue = value
        isCustomEquipment = !validateEquipment(value)
        viewModel.updateEquipment(value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título del formulario
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
                    text = "Complete los datos del equipo que está inspeccionando. El formato del equipo debe ser 'CAEX XXX' donde XXX es el número de identificación (301-339, 365 o 366).",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo para el equipo (con formato CAEX XXX)
        LabeledTextField(
            value = equipmentValue,
            onValueChange = { updateEquipmentValue(it) },
            label = "Equipo (Formato: CAEX XXX)",
            isError = viewModel.equipmentError || isCustomEquipment,
            errorMessage = when {
                viewModel.equipmentError -> "El equipo es obligatorio"
                isCustomEquipment -> "Formato inválido o ID de CAEX no permitido (use 301-339, 365 o 366)"
                else -> ""
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Campo para el inspector
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

        // Campo para el supervisor
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

        // Campo para el horómetro
        NumericTextField(
            value = viewModel.inspection.horometer,
            onValueChange = { viewModel.updateHorometer(it) },
            label = "Horómetro",
            isError = viewModel.horometerError,
            errorMessage = "El horómetro es obligatorio",
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para continuar
        PrimaryButton(
            text = "Continuar",
            onClick = { viewModel.proceedToNextStage() },
            enabled = !viewModel.equipmentError && !viewModel.inspectorError &&
                    !viewModel.supervisorError && !viewModel.horometerError &&
                    !isCustomEquipment
        )
    }
}