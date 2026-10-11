package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria

fun Categoria.etiqueta(): String = when (this) {
    Categoria.SUPERIOR -> "Superior"
    Categoria.INFERIOR -> "Inferior"
    Categoria.CALZADO -> "Calzado"
    Categoria.ABRIGO -> "Abrigo"
    Categoria.ACCESORIO -> "Accesorio"
}