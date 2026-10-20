package com.example.cambiateapp.domain.model

/**
 * Lista cerrada de etiquetas invisibles que la IA puede poner a una prenda.
 * Al ser cerrada, la IA no inventa palabras distintas cada vez y las etiquetas se pueden comparar.
 * Los colores no están acá: ya los tiene cada prenda en su campo "colores".
 * Van sin tildes ni espacios a propósito, para que nunca falle la comparación.
 */
object VocabularioEtiquetas {

    val PERMITIDAS: Set<String> = linkedSetOf(
        // estilo
        "casual", "formal", "elegante", "deportivo", "urbano", "clasico", "moderno", "vintage",
        "retro", "minimalista", "bohemio", "romantico", "rockero", "sofisticado", "playero",
        "callejero", "juvenil", "sobrio", "canchero", "atemporal",
        // momento o lugar
        "diario", "trabajo", "oficina", "fiesta", "noche", "playa", "salida", "boda",
        "entrevista", "viaje", "gimnasio", "casa", "universidad", "cita", "campo", "montana",
        // abrigo y clima
        "abrigado", "liviano", "fresco", "calido", "impermeable", "cortaviento", "termico",
        "ventilado", "acolchado", "forrado", "respirable", "resistente", "grueso", "fino",
        // temporada
        "verano", "invierno", "entretiempo", "otono", "primavera",
        // aspecto y diseño
        "liso", "estampado", "rayado", "cuadrille", "neutro", "colorido", "brillante", "mate",
        "floreado", "geometrico", "leopardo", "lunares", "logo", "bordado", "texturado",
        "transparente", "degrade", "camuflado", "tejido", "afelpado",
        // calce y forma
        "holgado", "ajustado", "oversize", "entallado", "ancho", "recto", "elastico", "largo",
        "corto", "cropped", "acampanado", "asimetrico", "cinturaalta",
        // material
        "algodon", "jean", "cuero", "lana", "sintetico", "lino", "seda", "satin", "poliester",
        "nylon", "gamuza", "terciopelo", "encaje", "tricot", "polar", "rustico",
        // versatilidad y personalidad
        "versatil", "basico", "comodo", "llamativo", "combinable", "protagonista", "delicado",
        "practico", "atrevido", "discreto"
    )

    /** Cantidad máxima de etiquetas por prenda. Son invisibles, así que cuantas más mejor, con un tope. */
    const val MAXIMO_POR_PRENDA = 12
}