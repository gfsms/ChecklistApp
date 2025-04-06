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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tudominio.checklistapp.data.database.AppDatabase
import com.tudominio.checklistapp.data.repository.InspectionRepository
import com.tudominio.checklistapp.ui.components.InspectionInfoSummary
import com.tudominio.checklistapp.ui.components.InspectionStatsSummary
import com.tudominio.checklistapp.ui.components.NonConformitiesSummary
import com.tudominio.checklistapp.ui.components.PrimaryButton
import com.tudominio.checklistapp.ui.viewmodels.NewInspectionViewModel
import com.tudominio.checklistapp.utils.PdfGenerator
import kotlinx.coroutines.Dispatchers
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
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var recurringMessages by remember { mutableStateOf<List<String>>(emptyList()) }

    // Verificar si hay no conformidades recurrentes
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getDatabase(context).inspectionDao()
                val repository = InspectionRepository(dao)
                val messages = mutableListOf<String>()

                viewModel.inspection.items.forEach { item ->
                    item.questions.forEach { question ->
                        val answer = question.answer
                        if (answer != null && !answer.isConform) {
                            val similarIssues = repository.findRecurringNonConformities(
                                questionText = question.text,
                                itemName = item.name,
                                equipment = viewModel.inspection.equipment,
                                currentInspectionId = viewModel.inspection.id
                            )

                            if (similarIssues.isNotEmpty()) {
                                messages.add("La pregunta '${question.text}' en el ítem '${item.name}' también ha sido reportada como no conforme en inspecciones anteriores del equipo ${viewModel.inspection.equipment}.")
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (messages.isNotEmpty()) {
                        recurringMessages = messages
                        showRecurringDialog = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mostrar diálogo con no conformidades recurrentes
    if (showRecurringDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringDialog = false },
            title = { Text("Hallazgos Recurrentes Detectados") },
            text = {
                LazyColumn {
                    items(recurringMessages) { message ->
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

    // Estado de carga
    if (viewModel.isLoading || isGeneratingPdf) {
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
                    text = if (isGeneratingPdf) "Generando PDF..." else "Cargando...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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

            // Botón para generar PDF
            Button(
                onClick = {
                    isGeneratingPdf = true
                    coroutineScope.launch(Dispatchers.IO) {
                        val pdfFile = PdfGenerator.generateInspectionReport(context, viewModel.inspection)
                        withContext(Dispatchers.Main) {
                            isGeneratingPdf = false
                            if (pdfFile != null) {
                                sharePdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para finalizar la inspección
        PrimaryButton(
            text = "Finalizar Inspección",
            onClick = onFinish
        )
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

        // Imprimir información para debug
        android.util.Log.d("PDF_DEBUG", "PDF Path: ${pdfFile.absolutePath}")
        android.util.Log.d("PDF_DEBUG", "PDF exists: ${pdfFile.exists()}")
        android.util.Log.d("PDF_DEBUG", "PDF size: ${pdfFile.length()}")
        android.util.Log.d("PDF_DEBUG", "URI: $uri")

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
        android.util.Log.e("PDF_ERROR", "Error: ${e.message}")
        Toast.makeText(context, "Error al compartir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}