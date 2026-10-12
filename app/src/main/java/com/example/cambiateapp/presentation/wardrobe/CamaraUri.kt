package com.example.cambiateapp.presentation.wardrobe

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Crea un archivo vacío en la caché de la app y devuelve su Uri, para que la cámara guarde ahí la foto. */
fun crearUriParaCamara(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File.createTempFile("prenda_", ".jpg", carpeta)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}