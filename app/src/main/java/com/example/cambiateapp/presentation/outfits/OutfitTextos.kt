package com.example.cambiateapp.presentation.outfits

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.Categoria

private val ORDEN_TEMPORADAS = listOf("primavera", "verano", "otono", "invierno")

@StringRes
private fun temporadaRes(clave: String): Int = when (clave) {
    "primavera" -> R.string.temporada_primavera
    "verano" -> R.string.temporada_verano
    "otono" -> R.string.temporada_otono
    else -> R.string.temporada_invierno
}

fun OutfitConPrendas.temporadasComunes(): List<String> {
    val conjuntos = prendas
        .map { prenda -> ORDEN_TEMPORADAS.filter { it in prenda.etiquetas }.toSet() }
        .filter { it.isNotEmpty() }
    if (conjuntos.isEmpty()) return emptyList()

    val comunes = conjuntos.reduce { a, b -> a intersect b }
    return ORDEN_TEMPORADAS.filter { it in comunes }
}


@Composable
fun textoDeTemporadas(claves: List<String>): String? = when {
    claves.isEmpty() -> null
    claves.size == ORDEN_TEMPORADAS.size -> stringResource(R.string.temporada_todo_el_anio)
    else -> claves.map { stringResource(temporadaRes(it)) }.joinToString(" / ")
}

fun iconoPorTemporada(claves: List<String>): ImageVector = when {
    claves.size == ORDEN_TEMPORADAS.size -> Icons.Rounded.Thermostat
    "invierno" in claves && "verano" !in claves && "primavera" !in claves -> Icons.Rounded.AcUnit
    "verano" in claves || "primavera" in claves -> Icons.Rounded.WbSunny
    else -> Icons.Rounded.Thermostat
}

@Composable
fun OutfitConPrendas.nombreVisibleLocalizado(): String {
    val superior = prendas.firstOrNull { it.categoria == Categoria.SUPERIOR }?.nombre
    val inferior = prendas.firstOrNull { it.categoria == Categoria.INFERIOR }?.nombre
    return when {
        superior != null && inferior != null ->
            stringResource(R.string.outfit_nombre_combinado, superior, inferior)
        superior != null -> superior
        inferior != null -> inferior
        else -> stringResource(R.string.outfit_generico)
    }
}