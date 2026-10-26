package com.example.cambiateapp.domain.model

data class ConsejoClima(
    val titulo: String,
    val mensaje: String,
    val prendasSugeridas: List<Prenda>,
    val tipo: TipoConsejo? = null,
)