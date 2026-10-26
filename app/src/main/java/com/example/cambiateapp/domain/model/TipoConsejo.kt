package com.example.cambiateapp.domain.model

sealed interface TipoConsejo {
    data class Lluvia(val probLluviaPct: Int, val abrigo: String?) : TipoConsejo
    data class Frio(val minC: Int, val abrigo: String?) : TipoConsejo
    data class Calor(val maxC: Int) : TipoConsejo
    data object Agradable : TipoConsejo

    data object Prueba : TipoConsejo
}