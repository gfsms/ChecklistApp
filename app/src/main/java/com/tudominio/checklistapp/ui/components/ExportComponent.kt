package com.tudominio.checklistapp.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tudominio.checklistapp.data.model.Inspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

@Composable
fun ExportComponent(
    inspection: Inspection,
    onExportStarted: () -> Unit = {},
    onExportCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showExportDialog by remember { mutableStateOf(false) }

    // Button to open export dialog
    Button(
        onClick = { showExportDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exportar Resultados")
        }
    }

    // Export dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Resultados") },
            text = {
                Column {
                    Text("Seleccione el formato para exportar los resultados de la inspección de ${inspection.equipment}.")

                    Spacer(modifier = Modifier.height(16.dp))

                    // PDF Option
                    ExportOption(
                        icon = Icons.Default.PictureAsPdf,
                        title = "PDF",
                        description = "Documento con formato completo",
                        onClick = {
                            showExportDialog = false
                            coroutineScope.launch {
                                exportToPdf(context, inspection, onExportStarted, onExportCompleted)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // CSV Option
                    ExportOption(
                        icon = Icons.Default.TableChart,
                        title = "CSV",
                        description = "Datos en formato tabular",
                        onClick = {
                            showExportDialog = false
                            coroutineScope.launch {
                                exportToCsv(context, inspection, onExportStarted, onExportCompleted)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private suspend fun exportToPdf(
    context: Context,
    inspection: Inspection,
    onExportStarted: () -> Unit,
    onExportCompleted: () -> Unit
) {
    withContext(Dispatchers.Main) {
        onExportStarted()
    }

    try {
        // Use existing PDF generation utility
        withContext(Dispatchers.IO) {
            val pdfFile = com.tudominio.checklistapp.utils.PdfGenerator.generateInspectionReport(
                context, inspection
            )

            withContext(Dispatchers.Main) {
                pdfFile?.let { sharePdfFile(context, it) }
                onExportCompleted()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Error al exportar a PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            onExportCompleted()
        }
    }
}

private suspend fun exportToCsv(
    context: Context,
    inspection: Inspection,
    onExportStarted: () -> Unit,
    onExportCompleted: () -> Unit
) {
    withContext(Dispatchers.Main) {
        onExportStarted()
    }

    try {
        withContext(Dispatchers.IO) {
            // Create file
            val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val fileName = "Inspeccion_${inspection.equipment}_${inspection.date.format(dateFormatter)}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)

            // Write CSV content
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    // Write headers
                    writer.append("Equipo,Inspector,Supervisor,Horómetro,Fecha,Estado\n")

                    // Write basic inspection info
                    val formattedDate = inspection.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    writer.append("${inspection.equipment},${inspection.inspector},${inspection.supervisor},${inspection.horometer},${formattedDate},${if (inspection.isCompleted) "Completada" else "Pendiente"}\n\n")

                    // Write items header
                    writer.append("Item,Pregunta,Conformidad,Comentario\n")

                    // Write items and questions
                    inspection.items.forEach { item ->
                        item.questions.forEach { question ->
                            val conformity = when {
                                question.answer == null -> "Sin responder"
                                question.answer.isConform -> "Conforme"
                                else -> "No Conforme"
                            }

                            val comment = question.answer?.comment ?: ""

                            writer.append("${item.name},${question.text},${conformity},${comment}\n")
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                shareCSVFile(context, file)
                onExportCompleted()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Error al exportar a CSV: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            onExportCompleted()
        }
    }
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error al compartir PDF: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun shareCSVFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error al compartir CSV: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}