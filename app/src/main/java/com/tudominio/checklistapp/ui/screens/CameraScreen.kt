package com.tudominio.checklistapp.ui.screens

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.tudominio.checklistapp.ui.components.CameraPermissionHandler
import com.tudominio.checklistapp.utils.FileUtils
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Pantalla para capturar fotos con la cámara.
 *
 * @param onPhotoTaken Callback llamado cuando se toma una foto, proporcionando el URI de la foto
 * @param onNavigateBack Callback para volver a la pantalla anterior
 */
@Composable
fun CameraScreen(
    onPhotoTaken: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Envolvemos todo el contenido de la cámara en el gestor de permisos
    CameraPermissionHandler(
        onPermissionsGranted = { /* Los permisos están concedidos */ },
        content = {
            CameraContent(
                onPhotoTaken = onPhotoTaken,
                onNavigateBack = onNavigateBack
            )
        }
    )
}

@Composable
private fun CameraContent(
    onPhotoTaken: (Uri) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCapturing by remember { mutableStateOf(false) }

    // Estado para almacenar los componentes de la cámara
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Configurar la cámara cuando el composable entre en la composición
    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()

        // Conectar la cámara al ciclo de vida y configurar la vista previa
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA, // Usar cámara trasera
            preview,
            imageCapture
        )

        // Conectar la vista previa al componente de vista previa
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // UI para la cámara
    Box(modifier = Modifier.fillMaxSize()) {
        // Vista previa de la cámara
        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Botón para volver atrás
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Botón para tomar foto
        IconButton(
            onClick = {
                if (!isCapturing) {
                    isCapturing = true
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        executor = ContextCompat.getMainExecutor(context),
                        onPhotoTaken = {
                            onPhotoTaken(it)
                            isCapturing = false
                        },
                        onError = {
                            // Manejar error aquí
                            isCapturing = false
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(80.dp)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Tomar Foto",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Función para capturar una foto con la cámara.
 */
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onPhotoTaken: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // Crear un archivo temporal para la foto
    val photoFile = FileUtils.createImageFile(context)

    // Configurar las opciones de salida para la captura
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // Tomar la foto
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            // Foto guardada exitosamente, ahora convertimos el archivo a URI de contenido
            val uri = FileUtils.getUriForFile(context, photoFile)
            onPhotoTaken(uri)
        }

        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }
    })
}

/**
 * Función de extensión para obtener el proveedor de cámara.
 */
private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}