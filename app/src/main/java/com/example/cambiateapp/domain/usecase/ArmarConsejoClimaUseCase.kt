package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.domain.model.Prenda
import javax.inject.Inject
import kotlin.math.roundToInt

class ArmarConsejoClimaUseCase @Inject constructor() {

    operator fun invoke(pronostico: Pronostico, prendas: List<Prenda>): ConsejoClima {
        val abrigos = prendas.filter { it.categoria == Categoria.ABRIGO }

        return when {
            pronostico.probLluviaPct >= UMBRAL_LLUVIA_PCT -> {
                val abrigo = abrigos.firstOrNull { it.nombre.contains("impermeable", ignoreCase = true) }
                    ?: abrigos.firstOrNull()
                ConsejoClima(
                    titulo = "Mañana llueve",
                    mensaje = if (abrigo != null) {
                        "Hay ${pronostico.probLluviaPct}% de lluvia: usá tu ${abrigo.nombre}."
                    } else {
                        "Hay ${pronostico.probLluviaPct}% de lluvia: llevá paraguas o una campera impermeable."
                    },
                    prendasSugeridas = listOfNotNull(abrigo),
                )
            }
            pronostico.tempMinC < UMBRAL_FRIO_C -> {
                val abrigo = abrigos.firstOrNull()
                ConsejoClima(
                    titulo = "Mañana hace frío",
                    mensaje = if (abrigo != null) {
                        "La mínima será de ${pronostico.tempMinC.roundToInt()}°: abrigate con tu ${abrigo.nombre}."
                    } else {
                        "La mínima será de ${pronostico.tempMinC.roundToInt()}°: abrigate bien."
                    },
                    prendasSugeridas = listOfNotNull(abrigo),
                )
            }
            pronostico.tempMaxC >= UMBRAL_CALOR_C -> ConsejoClima(
                titulo = "Mañana hace calor",
                mensaje = "La máxima será de ${pronostico.tempMaxC.roundToInt()}°: elegí ropa liviana.",
                prendasSugeridas = emptyList(),
            )
            else -> ConsejoClima(
                titulo = "Mañana, clima agradable",
                mensaje = "No necesitás abrigo especial. ¡Elegí el outfit que más te guste!",
                prendasSugeridas = emptyList(),
            )
        }
    }

    private companion object {
        const val UMBRAL_LLUVIA_PCT = 50
        const val UMBRAL_FRIO_C = 12.0
        const val UMBRAL_CALOR_C = 26.0
    }
}