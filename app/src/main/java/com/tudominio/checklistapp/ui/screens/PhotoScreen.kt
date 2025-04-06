package com.tudominio.checklistapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tudominio.checklistapp.R
import com.tudominio.checklistapp.data.model.Photo
import com.tudominio.checklistapp.ui.theme.Red

/**
 * Pantalla para gestionar las fotos de una pregunta no conforme.
 *
 * @param photos Lista de fotos asociadas a la pregunta
 * @param onAddPhoto Callback para añadir una nueva foto
 * @param onDeletePhoto Callback para eliminar una foto
 * @param onEditPhoto Callback para editar una foto (dibujar sobre ella)
 * @param onSaveChanges Callback para guardar los cambios y volver
 * @param onNavigateBack Callback para volver sin guardar cambios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    photos: List<Photo>,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    onEditPhoto: (Photo) -> Unit,
    onSaveChanges: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotos de Evidencia") },
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
                    IconButton(onClick = onSaveChanges) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPhoto,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Foto"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la sección
            Text(
                text = "Fotos disponibles (${photos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Si no hay fotos, mostrar mensaje
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay fotos. Presiona + para agregar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Galería en miniatura de las fotos
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos) { photo ->
                        PhotoThumbnail(
                            photo = photo,
                            isSelected = selectedPhoto == photo,
                            onClick = { selectedPhoto = photo }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Foto seleccionada en tamaño grande
                selectedPhoto?.let { photo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            // Foto
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Si la foto tiene dibujos, mostramos la versión con dibujos
                                val imageUri = if (photo.hasDrawings && photo.drawingUri != null) {
                                    photo.drawingUri
                                } else {
                                    photo.uri
                                }

                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(data = Uri.parse(imageUri))
                                            .error(R.drawable.app_logo)
                                            .placeholder(R.drawable.app_logo)
                                            .build()
                                    ),
                                    contentDescription = "Foto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentScale = ContentScale.Fit
                                )

                                // Botones de acción flotantes sobre la foto
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Botón para editar (dibujar sobre la foto)
                                    IconButton(
                                        onClick = { onEditPhoto(photo) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    // Botón para eliminar
                                    IconButton(
                                        onClick = {
                                            onDeletePhoto(photo)
                                            if (selectedPhoto == photo) {
                                                selectedPhoto = photos.firstOrNull { it != photo }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Red,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            // Información de la foto
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Fecha: ${photo.timestamp}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Si hay dibujos sobre la foto, mostramos un indicador
                                if (photo.hasDrawings) {
                                    Text(
                                        text = "Esta foto contiene anotaciones",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    // Si no hay foto seleccionada, mostrar mensaje
                    if (photos.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Selecciona una foto para ver en detalle",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra una miniatura de foto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoThumbnail(
    photo: Photo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(if (isSelected) 0.dp else 2.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Si la foto tiene dibujos, mostramos la versión con dibujos
            val imageUri = if (photo.hasDrawings && photo.drawingUri != null) {
                photo.drawingUri
            } else {
                photo.uri
            }

            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = Uri.parse(imageUri))
                        .error(R.drawable.app_logo)
                        .placeholder(R.drawable.app_logo)
                        .build()
                ),
                contentDescription = "Miniatura",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Indicador de que la foto tiene dibujos
            if (photo.hasDrawings) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Tiene anotaciones",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}