package com.tudominio.checklistapp.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

/**
 * Representa una línea de dibujo con color y puntos.
 */
data class DrawingLine(
    val color: Color,
    val strokeWidth: Float,
    val points: MutableList<Offset> = mutableListOf()
)

/**
 * Componente que permite dibujar sobre una imagen.
 *
 * @param imageUri URI de la imagen de fondo
 * @param currentColor Color actual del pincel
 * @param strokeWidth Grosor del pincel
 * @param onBitmapCreated Callback que se llama cuando se genera un nuevo bitmap con los dibujos
 */
@Composable
fun DrawingCanvas(
    imageUri: String,
    currentColor: Color,
    strokeWidth: Float = 5f,
    onBitmapCreated: (Bitmap) -> Unit
) {
    // Lista de líneas dibujadas
    val lines = remember { mutableStateListOf<DrawingLine>() }

    // Línea actual que se está dibujando
    var currentLine by remember { mutableStateOf<DrawingLine?>(null) }

    // Cargar la imagen de fondo
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUri)
            .build()
    )

    // Contenedor que contiene la imagen y el lienzo de dibujo
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painter,
            contentDescription = "Imagen para editar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Lienzo para dibujar
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentColor, strokeWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentLine = DrawingLine(
                                color = currentColor,
                                strokeWidth = strokeWidth
                            ).apply {
                                points.add(offset)
                            }
                            lines.add(currentLine!!)
                        },
                        onDrag = { change, _ ->
                            val current = currentLine
                            if (current != null) {
                                current.points.add(change.position)
                                // Forzar una recomposición
                                currentLine = current
                            }
                        },
                        onDragEnd = {
                            // Cuando se termina de dibujar, guardamos el bitmap resultante
                            val bitmap = createBitmapFromCanvas(
                                300,
                                300,
                                null,
                                lines
                            )
                            bitmap?.let { onBitmapCreated(it) }
                        }
                    )
                }
        ) {
            // Dibujamos todas las líneas guardadas
            lines.forEach { line ->
                if (line.points.size > 1) {
                    for (i in 0 until line.points.size - 1) {
                        drawLine(
                            color = line.color,
                            start = line.points[i],
                            end = line.points[i + 1],
                            strokeWidth = line.strokeWidth,
                            cap = StrokeCap.Round,
                            pathEffect = null
                        )
                    }
                }
            }
        }
    }
}

/**
 * Crea un bitmap combinando la imagen original con los dibujos realizados.
 */
private fun createBitmapFromCanvas(
    width: Int,
    height: Int,
    originalBitmap: Bitmap?,
    lines: List<DrawingLine>
): Bitmap? {
    if (width <= 0 || height <= 0) return null

    // Si no tenemos un bitmap original, creamos uno nuevo
    val bitmap = originalBitmap ?: Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    // Canvas para dibujar sobre el bitmap
    val canvas = android.graphics.Canvas(bitmap)

    // Configurar el pincel para dibujar
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // Dibujar cada línea
    lines.forEach { line ->
        paint.color = line.color.toArgb()
        paint.strokeWidth = line.strokeWidth

        if (line.points.size > 1) {
            val path = Path()
            path.moveTo(line.points.first().x, line.points.first().y)

            for (i in 1 until line.points.size) {
                path.lineTo(line.points[i].x, line.points[i].y)
            }

            canvas.drawPath(path, paint)
        }
    }

    return bitmap
}

/**
 * Extensión para convertir un Color de Compose a un color entero Android.
 */
fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}