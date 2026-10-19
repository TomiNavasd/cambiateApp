package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.AgendadorFake
import com.example.cambiateapp.RecordatorioRepositoryFake
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurarRecordatorioUseCaseTest {

    private val repositorio = RecordatorioRepositoryFake()
    private val agendador = AgendadorFake()
    private val caso = ConfigurarRecordatorioUseCase(repositorio, agendador)

    @Test
    fun `si esta activo guarda y agenda a la hora elegida`() = runTest {
        caso(PreferenciasRecordatorio(activo = true, hora = 7, minuto = 30))

        assertEquals(PreferenciasRecordatorio(true, 7, 30), repositorio.estado.value)
        assertEquals(7 to 30, agendador.programado)
    }

    @Test
    fun `si esta apagado guarda y cancela el aviso`() = runTest {
        caso(PreferenciasRecordatorio(activo = false, hora = 9, minuto = 0))

        assertEquals(PreferenciasRecordatorio(false, 9, 0), repositorio.estado.value)
        assertEquals(1, agendador.cancelaciones)
        assertNull(agendador.programado)
    }

    @Test
    fun `una hora invalida da error y no guarda ni agenda nada`() = runTest {
        val resultado = runCatching {
            caso(PreferenciasRecordatorio(activo = true, hora = 25, minuto = 0))
        }

        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
        assertEquals(PreferenciasRecordatorio(), repositorio.estado.value)
        assertNull(agendador.programado)
    }
}