package com.tudominio.checklistapp.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

data class DrawingLine(
    val color: Color,
    val strokeWidth: Float,
    val points: MutableList<Offset> = mutableListOf()
)

@Composable
fun DrawingCanvas(
    imageUri: String,
    currentColor: Color,
    strokeWidth: Float = 5f,
    onBitmapCreated: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lines = remember { mutableStateListOf<DrawingLine>() }
    var currentLine by remember { mutableStateOf<DrawingLine?>(null) }
    var forceRedraw by remember { mutableStateOf(0) }

    // Para mantener un seguimiento del tamaño real de la imagen
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var originalSize by remember { mutableStateOf(Size.Zero) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .build()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painter,
            contentDescription = "Imagen para editar",
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size.toSize()
                },
            contentScale = ContentScale.Fit
        )

        // Intentamos obtener el tamaño original de la imagen
        val originalBitmap = remember(imageUri) {
            try {
                getBitmapFromUri(context, imageUri)?.also {
                    originalSize = Size(it.width.toFloat(), it.height.toFloat())
                }
            } catch (e: Exception) {
                null
            }
        }

        // Lienzo para dibujar
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
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
                                forceRedraw++  // Forzar recomposición
                            }
                        },
                        onDragEnd = {
                            try {
                                // Cargamos la imagen original
                                val bitmap = getBitmapFromUri(context, imageUri)
                                if (bitmap != null) {
                                    val scaledBitmap = createBitmapWithScaledDrawings(
                                        bitmap,
                                        lines,
                                        imageSize,
                                        Size(bitmap.width.toFloat(), bitmap.height.toFloat())
                                    )
                                    onBitmapCreated(scaledBitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
        ) {
            // Este dibujo se volverá a ejecutar cada vez que forceRedraw cambie
            forceRedraw.let {
                // Dibujamos todas las líneas guardadas
                lines.forEach { line ->
                    if (line.points.size > 1) {
                        for (i in 0 until line.points.size - 1) {
                            drawLine(
                                color = line.color,
                                start = line.points[i],
                                end = line.points[i + 1],
                                strokeWidth = line.strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getBitmapFromUri(context: android.content.Context, uriString: String): Bitmap? {
    return try {
        val uri = android.net.Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun createBitmapWithScaledDrawings(
    originalBitmap: Bitmap,
    lines: List<DrawingLine>,
    displaySize: Size,
    originalSize: Size
): Bitmap {
    // Crear una copia del bitmap original para dibujar
    val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)

    // Configurar el pincel para dibujar
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // Calcular factores de escala
    val scaleX = originalSize.width / displaySize.width
    val scaleY = originalSize.height / displaySize.height

    // Dibujar cada línea
    lines.forEach { line ->
        paint.color = line.color.toArgb()
        paint.strokeWidth = line.strokeWidth * scaleX // Ajustar el ancho del trazo

        if (line.points.size > 1) {
            val path = Path()

            // Escalar el primer punto
            val firstPoint = line.points.first()
            val scaledFirstX = firstPoint.x * scaleX
            val scaledFirstY = firstPoint.y * scaleY
            path.moveTo(scaledFirstX, scaledFirstY)

            // Escalar y agregar el resto de puntos
            for (i in 1 until line.points.size) {
                val point = line.points[i]
                val scaledX = point.x * scaleX
                val scaledY = point.y * scaleY
                path.lineTo(scaledX, scaledY)
            }

            canvas.drawPath(path, paint)
        }
    }

    return resultBitmap
}

fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}