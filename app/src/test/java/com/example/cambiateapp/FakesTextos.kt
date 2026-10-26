package com.example.cambiateapp

import com.example.cambiateapp.presentation.common.ProveedorDeTextos

class TextosFake(private val idioma: String = "es") : ProveedorDeTextos {
    override fun obtener(id: Int, vararg args: Any): String =
        if (args.isEmpty()) "#$id" else "#$id:" + args.joinToString(",")

    override fun idioma(): String = idioma
}