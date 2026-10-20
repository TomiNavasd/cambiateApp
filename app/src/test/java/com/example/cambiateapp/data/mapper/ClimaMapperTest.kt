package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.ClimaDto
import com.example.cambiateapp.data.remote.DailyDto
import com.example.cambiateapp.domain.model.ClimaNoDisponibleException
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimaMapperTest {

    private fun dto(
        minimas: List<Double?>? = listOf(10.0, 8.0),
        maximas: List<Double?>? = listOf(20.0, 15.0),
        lluvia: List<Int?>? = listOf(10, 70),
    ) = ClimaDto(DailyDto(minimas, maximas, lluvia))

    @Test
    fun `toma los datos de mañana y no los de hoy`() {
        val pronostico = dto().aPronosticoDeManana()

        assertEquals(8.0, pronostico.tempMinC, 0.001)
        assertEquals(15.0, pronostico.tempMaxC, 0.001)
        assertEquals(70, pronostico.probLluviaPct)
    }

    @Test
    fun `si la probabilidad de lluvia es null asume cero`() {
        val pronostico = dto(lluvia = listOf(10, null)).aPronosticoDeManana()

        assertEquals(0, pronostico.probLluviaPct)
    }

    @Test(expected = ClimaNoDisponibleException::class)
    fun `si faltan las temperaturas de mañana falla con error del dominio`() {
        dto(minimas = listOf(10.0), maximas = listOf(20.0)).aPronosticoDeManana()
    }

    @Test(expected = ClimaNoDisponibleException::class)
    fun `si la respuesta no trae bloque diario falla con error del dominio`() {
        ClimaDto(daily = null).aPronosticoDeManana()
    }
}