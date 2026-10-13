package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.OutfitDto
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.google.firebase.Timestamp
import java.time.Instant

fun OutfitDto.toDomain(): Outfit? {
    val ocasionDominio = ocasion.toOcasionOrNull() ?: return null
    return Outfit(
        id = id,
        ocasion = ocasionDominio,
        prendasIds = prendasIds,
        fecha = fecha?.let { Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) }
            ?: Instant.EPOCH,
        favorito = favorito
    )
}

fun Outfit.toDto(userId: String): OutfitDto = OutfitDto(
    id = id,
    userId = userId,
    ocasion = ocasion.name.lowercase(),
    prendasIds = prendasIds,
    fecha = Timestamp(fecha.epochSecond, fecha.nano),
    favorito = favorito
)

private fun String.toOcasionOrNull(): Ocasion? =
    Ocasion.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }