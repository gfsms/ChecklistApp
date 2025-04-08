package com.tudominio.checklistapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.R
import com.tudominio.checklistapp.ui.components.MenuCardButton
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pantalla principal que muestra las opciones principales de la aplicación:
 * - Nueva Inspección: Para iniciar una nueva inspección de equipo
 * - Revisar Historial: Para ver el historial de inspecciones previas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewInspection: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    viewModel: NewInspectionViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checklist de Inspección CAEX") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo y título
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo de la aplicación",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Sistema de Gestión de Inspecciones CAEX",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Seleccione una opción para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Sección de botones principales
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón de Nueva Inspección
                    MenuCardButton(
                        title = "Nueva Inspección",
                        description = "Iniciar una nueva inspección de CAEX. Complete el formulario y registre conformidades y no conformidades en los 8 ítems de revisión.",
                        onClick = {
                            viewModel.resetInspection()
                            onNavigateToNewInspection()
                        }
                    )

                    // Botón de Revisar Historial
                    MenuCardButton(
                        title = "Revisar Historial",
                        description = "Consulte inspecciones anteriores, visualice estadísticas y genere reportes de inspecciones pasadas de los CAEX.",
                        onClick = onNavigateToHistory
                    )
                }

                // Debug Button - always show for development
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onNavigateToDebug,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Debug Base de Datos")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instrucciones de uso
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Instrucciones de Uso",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "1. Para iniciar una nueva inspección, seleccione 'Nueva Inspección'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "2. Complete la información básica del CAEX (equipo, inspector, supervisor, horómetro).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "3. Complete cada ítem del checklist, marcando como 'Conforme' o 'No Conforme' cada punto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "4. Para los puntos 'No Conformes', agregue comentarios y fotos como evidencia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = "5. Al finalizar, revise el resumen y genere el informe de inspección en PDF.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Pie de página
                Text(
                    text = "Versión 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}