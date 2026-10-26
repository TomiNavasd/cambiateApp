package com.example.cambiateapp.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ProveedorDeTextos {
    fun obtener(@StringRes id: Int, vararg args: Any): String

    fun idioma(): String
}

@Singleton
class ProveedorDeTextosAndroid @Inject constructor(
    @ApplicationContext private val context: Context
) : ProveedorDeTextos {

    override fun obtener(id: Int, vararg args: Any): String =
        if (args.isEmpty()) context.getString(id) else context.getString(id, *args)

    override fun idioma(): String {
        val codigo = context.resources.configuration.locales[0].language
        return if (codigo in IDIOMAS_SOPORTADOS) codigo else "es"
    }

    private companion object {
        val IDIOMAS_SOPORTADOS = setOf("es", "en", "pt")
    }
}