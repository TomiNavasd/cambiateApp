package com.example.cambiateapp.domain.model

data class AnalisisPrenda(
    val nombre: String,
    val descripcion: String,
    val categoria: Categoria?,
    val colores: List<String>,
    val ocasiones: List<Ocasion>,
    val etiquetas: List<String>
)