package com.example.cambiateapp.data.repository

import com.example.cambiateapp.data.remote.ClimaDto
import com.example.cambiateapp.data.remote.DailyDto
import com.example.cambiateapp.data.remote.OpenMeteoApi
import com.example.cambiateapp.domain.model.ClimaNoDisponibleException
import com.example.cambiateapp.domain.model.Coordenadas
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimaRepositoryImplTest {

    /** Una API de mentira: devuelve lo que le pidamos y recuerda con qué coordenadas la llamaron. */
    private class ApiFalsa(private val respuesta: () -> ClimaDto) : OpenMeteoApi {
        var latitudRecibida: Double? = null
        var longitudRecibida: Double? = null

        override suspend fun pronostico(latitud: Double, longitud: Double): ClimaDto {
            latitudRecibida = latitud
            longitudRecibida = longitud
            return respuesta()
        }
    }

    private val buenosAires = Coordenadas(latitud = -34.6, longitud = -58.38)

    @Test
    fun `devuelve el pronostico de mañana y manda las coordenadas a la API`() = runTest {
        val api = ApiFalsa {
            ClimaDto(DailyDto(listOf(10.0, 8.0), listOf(20.0, 15.0), listOf(10, 70)))
        }
        val repositorio = ClimaRepositoryImpl(api)

        val pronostico = repositorio.pronosticoDeManana(buenosAires)

        assertEquals(8.0, pronostico.tempMinC, 0.001)
        assertEquals(70, pronostico.probLluviaPct)
        assertEquals(-34.6, api.latitudRecibida!!, 0.001)
        assertEquals(-58.38, api.longitudRecibida!!, 0.001)
    }

    @Test(expected = ClimaNoDisponibleException::class)
    fun `si no hay conexion falla con error del dominio`() = runTest {
        val repositorio = ClimaRepositoryImpl(ApiFalsa { throw IOException("sin red") })

        repositorio.pronosticoDeManana(buenosAires)
    }
}