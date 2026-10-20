package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria

fun Categoria.etiqueta(): String = when (this) {
    Categoria.SUPERIOR -> "Tops"
    Categoria.INFERIOR -> "Pantalones"
    Categoria.CALZADO -> "Calzado"
    Categoria.ABRIGO -> "Camperas"
    Categoria.ACCESORIO -> "Accesorios"
}