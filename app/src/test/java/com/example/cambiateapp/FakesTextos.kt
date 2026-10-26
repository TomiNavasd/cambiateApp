package com.example.cambiateapp

import com.example.cambiateapp.presentation.common.ProveedorDeTextos

class TextosFake : ProveedorDeTextos {
    override fun obtener(id: Int, vararg args: Any): String =
        if (args.isEmpty()) "#$id" else "#$id:" + args.joinToString(",")
}