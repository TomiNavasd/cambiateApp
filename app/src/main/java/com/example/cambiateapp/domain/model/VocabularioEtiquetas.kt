package com.example.cambiateapp.domain.model

/**
 * Lista cerrada de etiquetas que la IA puede poner a una prenda.
 * Al ser cerrada, la IA no inventa palabras distintas cada vez y las etiquetas se pueden comparar.
 */
object VocabularioEtiquetas {

    val PERMITIDAS: Set<String> = linkedSetOf(
        // estilo
        "casual", "formal", "elegante", "deportivo", "urbano",
        // abrigo y clima
        "abrigado", "liviano", "impermeable",
        // temporada
        "verano", "invierno", "entretiempo",
        // aspecto
        "liso", "estampado", "rayado", "neutro", "colorido",
        // calce
        "holgado", "ajustado"
    )

    /** Cantidad máxima de etiquetas por prenda: más que eso es ruido para la IA de outfits. */
    const val MAXIMO_POR_PRENDA = 6
}