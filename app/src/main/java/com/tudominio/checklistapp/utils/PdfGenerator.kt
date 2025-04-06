package com.tudominio.checklistapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.tudominio.checklistapp.R
import com.tudominio.checklistapp.data.model.Inspection
import com.tudominio.checklistapp.data.model.InspectionItem
import com.tudominio.checklistapp.data.model.InspectionQuestion
import com.tudominio.checklistapp.data.model.Answer
import com.tudominio.checklistapp.data.model.Photo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object PdfGenerator {

    fun generateInspectionReport(context: Context, inspection: Inspection): File? {
        try {
            // Crear archivo temporal para el PDF
            val file = File(context.getExternalFilesDir(null), "Inspeccion_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)

            // Configurar documento PDF
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // Añadir logo
            addLogo(context, document)

            // Añadir título y encabezado
            addHeader(document, inspection)

            // Añadir información básica
            addBasicInfo(document, inspection)

            // Añadir no conformidades
            addNonConformities(context, document, inspection)

            // Cerrar documento
            document.close()
            outputStream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addLogo(context: Context, document: Document) {
        try {
            // Cargar logo desde recursos
            val inputStream = context.resources.openRawResource(R.drawable.app_logo)
            val logoBytes = inputStream.readBytes()
            val logo = Image.getInstance(logoBytes)

            // Configurar tamaño y posición
            logo.scaleToFit(70f, 70f)
            logo.alignment = Element.ALIGN_RIGHT

            document.add(logo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addHeader(document: Document, inspection: Inspection) {
        // Título principal
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor.DARK_GRAY)
        val title = Paragraph("INFORME DE INSPECCIÓN", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        // Subtítulo con fecha
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val formattedDate = inspection.date.format(dateFormatter)
        val subtitleFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)
        val subtitle = Paragraph("Fecha: $formattedDate", subtitleFont)
        subtitle.alignment = Element.ALIGN_CENTER
        document.add(subtitle)

        document.add(Paragraph(" "))
    }

    private fun addBasicInfo(document: Document, inspection: Inspection) {
        val sectionFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        val sectionTitle = Paragraph("INFORMACIÓN GENERAL", sectionFont)
        document.add(sectionTitle)
        document.add(Paragraph(" "))

        // Tabla para información básica
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 2f))

        val labelFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
        val valueFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)

        addTableRow(table, "Equipo:", inspection.equipment, labelFont, valueFont)
        addTableRow(table, "Inspector:", inspection.inspector, labelFont, valueFont)
        addTableRow(table, "Supervisor:", inspection.supervisor, labelFont, valueFont)
        addTableRow(table, "Horómetro:", inspection.horometer, labelFont, valueFont)

        document.add(table)
        document.add(Paragraph(" "))
    }

    private fun addTableRow(table: PdfPTable, label: String, value: String, labelFont: Font, valueFont: Font) {
        val labelCell = PdfPCell(Phrase(label, labelFont))
        labelCell.border = Rectangle.NO_BORDER
        labelCell.paddingBottom = 5f
        table.addCell(labelCell)

        val valueCell = PdfPCell(Phrase(value, valueFont))
        valueCell.border = Rectangle.NO_BORDER
        valueCell.paddingBottom = 5f
        table.addCell(valueCell)
    }

    private fun addNonConformities(context: Context, document: Document, inspection: Inspection) {
        val sectionFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        val sectionTitle = Paragraph("NO CONFORMIDADES", sectionFont)
        document.add(sectionTitle)
        document.add(Paragraph(" "))

        // Verificar si hay no conformidades
        val hasNonConformities = inspection.items.any { item ->
            item.questions.any { q ->
                val answer = q.answer
                answer != null && !answer.isConform
            }
        }

        if (!hasNonConformities) {
            val noIssuesFont = Font(Font.FontFamily.HELVETICA, 12f, Font.ITALIC)
            val noIssues = Paragraph("No se encontraron no conformidades.", noIssuesFont)
            document.add(noIssues)
            return
        }

        // Recorrer ítems y preguntas
        inspection.items.forEachIndexed { index, item ->
            val nonConformQuestions = item.questions.filter { q ->
                val answer = q.answer
                answer != null && !answer.isConform
            }

            if (nonConformQuestions.isNotEmpty()) {
                addItemTitle(document, item, index + 1)

                nonConformQuestions.forEach { question ->
                    addQuestionDetails(context, document, question)
                }
            }
        }
    }

    private fun addItemTitle(document: Document, item: InspectionItem, itemNumber: Int) {
        val itemFont = Font(Font.FontFamily.HELVETICA, 13f, Font.BOLDITALIC, BaseColor(0, 0, 150))
        val itemTitle = Paragraph(
            "Ítem $itemNumber: ${item.name}",
            itemFont
        )
        document.add(itemTitle)
    }

    private fun addQuestionDetails(context: Context, document: Document, question: InspectionQuestion) {
        val answer = question.answer ?: return

        // Añadir pregunta
        val questionFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor(200, 0, 0))
        val questionText = Paragraph(
            "• ${question.text}",
            questionFont
        )
        document.add(questionText)

        // Añadir comentario
        if (answer.comment.isNotBlank()) {
            val commentFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
            val commentText = Paragraph(
                "Comentario: ${answer.comment}",
                commentFont
            )
            commentText.indentationLeft = 20f
            document.add(commentText)
        }

        // Añadir fotos
        if (answer.photos.isNotEmpty()) {
            val photoTitleFont = Font(Font.FontFamily.HELVETICA, 11f, Font.ITALIC)
            document.add(Paragraph("Evidencias fotográficas:", photoTitleFont))

            val photoTable = PdfPTable(2)
            photoTable.widthPercentage = 100f
            photoTable.setWidths(floatArrayOf(1f, 1f))

            answer.photos.forEach { photo ->
                try {
                    val photoUri = if (photo.hasDrawings && photo.drawingUri != null) {
                        photo.drawingUri
                    } else {
                        photo.uri
                    }

                    val bitmap = getBitmapFromUri(context, photoUri)
                    if (bitmap != null) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        val imageBytes = stream.toByteArray()

                        val image = Image.getInstance(imageBytes)
                        image.scaleToFit(250f, 250f)

                        val cell = PdfPCell(image)
                        cell.borderWidth = 0.5f
                        cell.paddingBottom = 5f
                        cell.horizontalAlignment = Element.ALIGN_CENTER
                        photoTable.addCell(cell)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    photoTable.addCell(PdfPCell(Phrase("Error al cargar imagen")))
                }
            }

            // Si hay un número impar de fotos, añadimos una celda vacía
            if (answer.photos.size % 2 != 0) {
                photoTable.addCell(PdfPCell())
            }

            document.add(photoTable)
        }

        document.add(Paragraph(" "))
    }

    private fun getBitmapFromUri(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}