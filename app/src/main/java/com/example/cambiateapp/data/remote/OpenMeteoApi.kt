package com.example.cambiateapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    /** Pide hoy y mañana. Open-Meteo no necesita clave de API. */
    @GET("v1/forecast?daily=temperature_2m_min,temperature_2m_max,precipitation_probability_max&timezone=auto&forecast_days=2")
    suspend fun pronostico(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
    ): ClimaDto
}