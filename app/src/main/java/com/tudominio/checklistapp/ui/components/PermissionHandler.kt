package com.tudominio.checklistapp.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Componente simplificado que gestiona la solicitud de permisos de cámara.
 *
 * @param onPermissionsGranted Callback cuando todos los permisos son concedidos
 * @param content Contenido a mostrar cuando los permisos son concedidos
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    onPermissionsGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    // Solo pediremos el permiso de cámara para simplificar
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // En Android 10+ los permisos de almacenamiento para la cámara
    // suelen estar concedidos para el almacenamiento privado de la app
    val cameraPermissionGranted = cameraPermissionState.status.isGranted

    if (cameraPermissionGranted) {
        // Si el permiso está concedido, mostramos el contenido
        LaunchedEffect(Unit) {
            onPermissionsGranted()
        }
        content()
    } else {
        // Si no, mostramos la pantalla de solicitud de permisos
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Para tomar fotos, necesitamos acceder a la cámara.",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }
            ) {
                Text("Solicitar Permiso de Cámara")
            }

            // Si el permiso fue denegado permanentemente, mostramos un diálogo adicional
            if (cameraPermissionState.status.shouldShowRationale) {
                AlertDialog(
                    onDismissRequest = { /* No hacer nada */ },
                    title = { Text("Permiso Necesario") },
                    text = {
                        Text(
                            "La cámara es necesaria para esta función. " +
                                    "Por favor, habilita este permiso en la configuración de la aplicación."
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            cameraPermissionState.launchPermissionRequest()
                        }) {
                            Text("Solicitar de Nuevo")
                        }
                    }
                )
            }
        }
    }
}