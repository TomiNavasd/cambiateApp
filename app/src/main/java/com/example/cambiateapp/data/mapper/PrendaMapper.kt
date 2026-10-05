package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.PrendaDto
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda

/** Devuelve null si el documento tiene una categoría desconocida (dato corrupto). */
fun PrendaDto.toDomain(): Prenda? {
    val categoriaDominio = categoria.toCategoriaOrNull() ?: return null
    return Prenda(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        categoria = categoriaDominio,
        colores = colores,
        ocasiones = ocasiones.mapNotNull { it.toOcasionOrNull() },
        fotoUrl = fotoUrl
    )
}

fun Prenda.toDto(userId: String): PrendaDto = PrendaDto(
    id = id,
    userId = userId,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria.name.lowercase(),
    colores = colores,
    ocasiones = ocasiones.map { it.name.lowercase() },
    fotoUrl = fotoUrl
)

private fun String.toCategoriaOrNull(): Categoria? =
    Categoria.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

private fun String.toOcasionOrNull(): Ocasion? =
    Ocasion.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }