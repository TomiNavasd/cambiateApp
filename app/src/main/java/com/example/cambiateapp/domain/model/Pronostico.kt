package com.example.cambiateapp.domain.model

data class Pronostico(
    val tempMinC: Double,
    val tempMaxC: Double,
    val probLluviaPct: Int,
    val obtenidoEnMs: Long? = null,
    val deCache: Boolean = false,
)