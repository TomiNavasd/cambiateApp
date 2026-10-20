package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Prenda

private val TEMPORADAS = linkedMapOf(
    "verano" to "Verano",
    "otono" to "Otoño",
    "invierno" to "Invierno",
    "primavera" to "Primavera",
    "entretiempo" to "Entretiempo"
)

/** Temporada que sugieren las etiquetas de la IA. Null si la prenda todavía no tiene ninguna. */
fun Prenda.temporadaEtiqueta(): String? {
    val encontradas = TEMPORADAS.filterKeys { it in etiquetas }.values.toList()
    return when {
        encontradas.isEmpty() -> null
        encontradas.size == 1 -> encontradas.first()
        else -> "Todo el año"
    }
}

/** Texto corto para la tarjeta, por ejemplo "Verde • Invierno". Null si no hay nada para mostrar. */
fun Prenda.detalleCorto(): String? {
    val partes = listOfNotNull(
        colores.firstOrNull()?.replaceFirstChar { it.titlecase() },
        temporadaEtiqueta()
    )
    return partes.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}