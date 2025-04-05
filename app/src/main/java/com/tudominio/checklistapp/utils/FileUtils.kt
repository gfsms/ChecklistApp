package com.tudominio.checklistapp.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clase utilitaria para gestionar archivos, especialmente para fotos.
 */
object FileUtils {

    /**
     * Crea un archivo temporal para guardar una foto.
     *
     * @param context El contexto de la aplicación
     * @return El archivo creado
     */
    fun createImageFile(context: Context): File {
        // Crear un nombre de archivo único basado en la fecha y hora
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        // Directorio de almacenamiento externo de la aplicación para fotos
        val storageDir = context.getExternalFilesDir("Pictures")

        // Crear el archivo
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    /**
     * Convierte un archivo en un URI de contenido usando FileProvider.
     *
     * @param context El contexto de la aplicación
     * @param file El archivo a convertir
     * @return Un URI de contenido para el archivo
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Elimina un archivo si existe.
     *
     * @param uri URI del archivo a eliminar
     * @param context El contexto de la aplicación
     * @return true si el archivo fue eliminado exitosamente, false en caso contrario
     */
    fun deleteFile(uri: String?, context: Context): Boolean {
        if (uri.isNullOrEmpty()) return false

        try {
            val file = File(uri)
            if (file.exists()) {
                return file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }
}