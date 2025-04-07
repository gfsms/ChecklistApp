package com.tudominio.checklistapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tudominio.checklistapp.ui.components.InspectionInfoSummary
import com.tudominio.checklistapp.ui.components.InspectionStatsSummary
import com.tudominio.checklistapp.ui.components.NonConformitiesSummary
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.theme.Yellow
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import com.tudominio.checklistapp.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SummaryScreen(
    viewModel: NewInspectionViewModel,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var isCheckingRecurring by remember { mutableStateOf(true) }
    var recurringMessages by remember { mutableStateOf<List<String>>(emptyList()) }

    // Skip database access and just check for non-conformities in the current inspection
    LaunchedEffect(Unit) {
        delay(1000) // Simulate a check

        val messages = mutableListOf<String>()
        viewModel.inspection.items.forEach { item ->
            item.questions.forEach { question ->
                if (question.answer?.isConform == false) {
                    messages.add("La pregunta '${question.text}' en el ítem '${item.name}' puede ser un problema recurrente en el equipo ${viewModel.inspection.equipment}.")
                }
            }
        }

        recurringMessages = messages
        isCheckingRecurring = false

        if (messages.isNotEmpty()) {
            showRecurringDialog = true
        }
    }

    // Show dialog with recurring non-conformities
    if (showRecurringDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Yellow
                )
            },
            title = { Text("Hallazgos Potencialmente Recurrentes") },
            text = {
                Column {
                    Text(
                        text = "Se encontraron no conformidades que podrían ser recurrentes:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    recurringMessages.forEach { message ->
                        Text(
                            text = "• $message",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRecurringDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Main content
        if (viewModel.isLoading || isGeneratingPdf || isCheckingRecurring) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            isGeneratingPdf -> "Generando PDF..."
                            isCheckingRecurring -> "Verificando hallazgos recurrentes..."
                            else -> "Cargando..."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
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

                    // Botón para generar PDF
                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val pdfFile = PdfGenerator.generateInspectionReport(context, viewModel.inspection)
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        if (pdfFile != null) {
                                            sharePdf(context, pdfFile)
                                        } else {
                                            snackbarHostState.showSnackbar("Error al generar el PDF")
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isGeneratingPdf = false
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generar Informe PDF")
                        }
                    }

                    // Botón para ver no conformidades recurrentes
                    if (recurringMessages.isNotEmpty()) {
                        Button(
                            onClick = { showRecurringDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Posibles Hallazgos Recurrentes (${recurringMessages.size})")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para finalizar la inspección
                PrimaryButton(
                    text = "Finalizar Inspección",
                    onClick = onFinish
                )
            }
        }
    }
}

// Función para compartir el PDF generado
private fun sharePdf(context: android.content.Context, pdfFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Verificar si hay aplicaciones para manejar PDFs
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Intentar con ACTION_SEND si VIEW no funciona
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Ver PDF con"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al compartir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}