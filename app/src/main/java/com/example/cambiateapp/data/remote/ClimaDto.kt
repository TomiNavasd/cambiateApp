package com.example.cambiateapp.data.remote

import com.google.gson.annotations.SerializedName

/** Forma de la respuesta de Open-Meteo (solo los campos que usamos). */
data class ClimaDto(
    val daily: DailyDto?,
)

data class DailyDto(
    @SerializedName("temperature_2m_min") val temperaturaMinima: List<Double?>?,
    @SerializedName("temperature_2m_max") val temperaturaMaxima: List<Double?>?,
    @SerializedName("precipitation_probability_max") val probabilidadLluviaMaxima: List<Int?>?,
)