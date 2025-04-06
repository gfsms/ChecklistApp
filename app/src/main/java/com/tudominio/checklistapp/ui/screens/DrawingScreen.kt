package com.tudominio.checklistapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tudominio.checklistapp.ui.components.DrawingCanvas
import com.tudominio.checklistapp.ui.theme.Red
import com.tudominio.checklistapp.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Pantalla para dibujar y hacer anotaciones en una foto.
 *
 * @param photoUri URI de la foto sobre la que se va a dibujar
 * @param onDrawingFinished Callback que se llama cuando se finaliza el dibujo, proporcionando el URI de la imagen con dibujos
 * @param onNavigateBack Callback para volver a la pantalla anterior sin guardar cambios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    photoUri: String,
    onDrawingFinished: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Estado para el color actual seleccionado
    var currentColor by remember { mutableStateOf(Color.Red) }

    // Estado para el grosor del pincel
    var strokeWidth by remember { mutableFloatStateOf(5f) }

    // Estado para el último bitmap generado (lo usaremos para guardar al final)
    var lastBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Lista de colores disponibles
    val colors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.White,
        Color.Black
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dibujar sobre la Foto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                ),
                actions = {
                    // Botón para guardar los cambios
                    IconButton(
                        onClick = {
                            // Guardamos el bitmap y devolvemos el URI
                            lastBitmap?.let { bitmap ->
                                val newUri = saveBitmap(context, bitmap)
                                if (newUri != null) {
                                    onDrawingFinished(newUri.toString())
                                }
                            } ?: onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Área de dibujo (ocupa la mayor parte de la pantalla)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DrawingCanvas(
                    imageUri = photoUri,
                    currentColor = currentColor,
                    strokeWidth = strokeWidth,
                    onBitmapCreated = { bitmap ->
                        lastBitmap = bitmap
                    }
                )
            }

            // Panel de herramientas (parte inferior)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                // Botón para borrar todos los dibujos
                Button(
                    onClick = {
                        // Simplemente usamos la imagen original como bitmap final
                        val originalBitmap = getBitmapFromUri(context, photoUri)
                        if (originalBitmap != null) {
                            lastBitmap = originalBitmap
                            // También podemos guardar inmediatamente
                            val newUri = saveBitmap(context, originalBitmap)
                            if (newUri != null) {
                                onDrawingFinished(newUri.toString())
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar dibujos",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Borrar dibujos")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de colores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        ColorSelector(
                            color = color,
                            isSelected = color == currentColor,
                            onClick = { currentColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control del grosor del pincel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grosor:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 1f..20f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    // Muestra del grosor actual
                    Box(
                        modifier = Modifier
                            .size(strokeWidth.dp)
                            .background(currentColor, CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * Intenta obtener un Bitmap a partir de un URI
 */
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

/**
 * Selector de color para el panel de herramientas.
 */
@Composable
fun ColorSelector(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

/**
 * Guarda el bitmap en un archivo y devuelve su URI.
 */
private fun saveBitmap(context: Context, bitmap: Bitmap): Uri? {
    try {
        val file = FileUtils.createImageFile(context)
        FileOutputStream(file).use { out ->
            // Usamos una mayor calidad para la imagen
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }

        // Asegurarse de que el archivo existe y tiene contenido
        if (file.exists() && file.length() > 0) {
            return FileUtils.getUriForFile(context, file)
        } else {
            return null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}