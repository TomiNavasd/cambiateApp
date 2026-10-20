package com.example.cambiateapp.presentation.outfits

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Nightlife
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion

private val TEMPORADAS = listOf(
    "primavera" to "Primavera",
    "verano" to "Verano",
    "otono" to "Otoño",
    "invierno" to "Invierno"
)

/** Ícono de cada ocasión para los chips. */
fun Ocasion.icono(): ImageVector = when (this) {
    Ocasion.TRABAJO -> Icons.Rounded.BusinessCenter
    Ocasion.FACULTAD -> Icons.Rounded.School
    Ocasion.SALIDA -> Icons.Rounded.Nightlife
    Ocasion.CASAMIENTO -> Icons.Rounded.Diamond
    Ocasion.DEPORTE -> Icons.Rounded.FitnessCenter
    Ocasion.CASUAL -> Icons.Rounded.Weekend
}

/** Nombre para mostrar, armado con la prenda superior y la inferior. */
fun OutfitConPrendas.nombreVisible(): String {
    val superior = prendas.firstOrNull { it.categoria == Categoria.SUPERIOR }?.nombre
    val inferior = prendas.firstOrNull { it.categoria == Categoria.INFERIOR }?.nombre
    return when {
        superior != null && inferior != null -> "$superior con $inferior"
        superior != null -> superior
        inferior != null -> inferior
        else -> "Outfit"
    }
}

/**
 * Temporadas que comparten todas las prendas del outfit que tienen etiqueta de temporada.
 * Null si no hay datos o si no coinciden en ninguna.
 */
fun OutfitConPrendas.temporadaTexto(): String? {
    val conjuntos = prendas
        .map { prenda -> TEMPORADAS.map { it.first }.filter { it in prenda.etiquetas }.toSet() }
        .filter { it.isNotEmpty() }
    if (conjuntos.isEmpty()) return null

    val comunes = conjuntos.reduce { a, b -> a intersect b }
    val nombres = TEMPORADAS.filter { it.first in comunes }.map { it.second }
    return when {
        nombres.isEmpty() -> null
        nombres.size == TEMPORADAS.size -> "Todo el año"
        else -> nombres.joinToString(" / ")
    }
}

/** Ícono según la temporada: nieve, sol o termómetro para las intermedias. */
fun iconoDeTemporada(texto: String): ImageVector = when {
    texto.contains("Invierno") && !texto.contains("Verano") && !texto.contains("Primavera") ->
        Icons.Rounded.AcUnit
    texto.contains("Verano") || texto.contains("Primavera") -> Icons.Rounded.WbSunny
    else -> Icons.Rounded.Thermostat
}