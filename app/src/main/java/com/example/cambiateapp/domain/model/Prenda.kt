package com.example.cambiateapp.domain.model

data class Prenda(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: Categoria,
    val colores: List<String>,
    val ocasiones: List<Ocasion>,
    val fotoUrl: String,
    /** Etiquetas que pone la IA al analizar la foto. El usuario no las ve: ayudan a armar mejores outfits. */
    val etiquetas: List<String> = emptyList()
) {
    val esNueva: Boolean get() = id.isEmpty()
}