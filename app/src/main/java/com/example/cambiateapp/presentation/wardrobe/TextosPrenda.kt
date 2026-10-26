package com.example.cambiateapp.presentation.wardrobe

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda

@StringRes
fun Categoria.etiquetaRes(): Int = when (this) {
    Categoria.SUPERIOR -> R.string.categoria_superior
    Categoria.INFERIOR -> R.string.categoria_inferior
    Categoria.CALZADO -> R.string.categoria_calzado
    Categoria.ABRIGO -> R.string.categoria_abrigo
    Categoria.ACCESORIO -> R.string.categoria_accesorio
}

@StringRes
fun Ocasion.etiquetaRes(): Int = when (this) {
    Ocasion.TRABAJO -> R.string.ocasion_trabajo
    Ocasion.FACULTAD -> R.string.ocasion_facultad
    Ocasion.SALIDA -> R.string.ocasion_salida
    Ocasion.CASAMIENTO -> R.string.ocasion_casamiento
    Ocasion.DEPORTE -> R.string.ocasion_deporte
    Ocasion.CASUAL -> R.string.ocasion_casual
}

private val TEMPORADAS_RES = linkedMapOf(
    "verano" to R.string.temporada_verano,
    "otono" to R.string.temporada_otono,
    "invierno" to R.string.temporada_invierno,
    "primavera" to R.string.temporada_primavera,
    "entretiempo" to R.string.temporada_entretiempo
)

@StringRes
fun Prenda.temporadaRes(): Int? {
    val encontradas = TEMPORADAS_RES.filterKeys { it in etiquetas }.values.toList()
    return when {
        encontradas.isEmpty() -> null
        encontradas.size == 1 -> encontradas.first()
        else -> R.string.temporada_todo_el_anio
    }
}


@Composable
fun Prenda.detalleCortoLocalizado(): String? {
    val temporada = temporadaRes()?.let { stringResource(it) }
    val partes = listOfNotNull(
        colores.firstOrNull()?.replaceFirstChar { it.titlecase() },
        temporada
    )
    return partes.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}