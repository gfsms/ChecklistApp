package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.ui.components.InspectionInfoSummary
import com.tudominio.checklistapp.ui.components.InspectionStatsSummary
import com.tudominio.checklistapp.ui.components.NonConformitiesSummary
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel

/**
 * Pantalla que muestra un resumen de la inspección realizada.
 * Incluye información básica, estadísticas y no conformidades encontradas.
 *
 * @param viewModel ViewModel que gestiona el estado de la inspección
 * @param onFinish Callback para finalizar la inspección
 */
@Composable
fun SummaryScreen(
    viewModel: NewInspectionViewModel,
    onFinish: () -> Unit
) {
    // Estado de carga
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Contenido principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Contenido scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la pantalla
            Text(
                text = "Resumen de Inspección",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // Información básica de la inspección
            InspectionInfoSummary(inspection = viewModel.inspection)

            // Estadísticas
            InspectionStatsSummary(inspection = viewModel.inspection)

            // No conformidades
            NonConformitiesSummary(items = viewModel.inspection.items)

            Spacer(modifier = Modifier.height(16.dp))

            // Nota sobre PDF
            Text(
                text = "Al finalizar la inspección, se generará un informe PDF con los detalles de la misma.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para finalizar la inspección
        PrimaryButton(
            text = "Finalizar Inspección",
            onClick = onFinish
        )
    }
}