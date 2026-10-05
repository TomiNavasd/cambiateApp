package com.example.cambiateapp.domain.model

data class Prenda(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: Categoria,
    val colores: List<String>,
    val ocasiones: List<Ocasion>,
    val fotoUrl: String
) {
    val esNueva: Boolean get() = id.isEmpty()
}