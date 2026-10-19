package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.model.Pronostico
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArmarConsejoClimaUseCaseTest {

    private val caso = ArmarConsejoClimaUseCase()

    private fun abrigo(nombre: String) = Prenda(
        id = nombre,
        nombre = nombre,
        descripcion = "",
        categoria = Categoria.ABRIGO,
        colores = emptyList(),
        ocasiones = emptyList(),
        fotoUrl = "",
    )

    @Test
    fun `con lluvia y abrigo sugiere el abrigo`() {
        val c = caso(Pronostico(15.0, 20.0, 80), listOf(abrigo("campera negra")))
        assertEquals("Mañana llueve", c.titulo)
        assertEquals(1, c.prendasSugeridas.size)
    }

    @Test
    fun `con lluvia prefiere la impermeable`() {
        val c = caso(
            Pronostico(15.0, 20.0, 80),
            listOf(abrigo("campera negra"), abrigo("campera impermeable")),
        )
        assertEquals("campera impermeable", c.prendasSugeridas.first().nombre)
    }

    @Test
    fun `con lluvia y sin abrigos igual da consejo`() {
        val c = caso(Pronostico(15.0, 20.0, 80), emptyList())
        assertTrue(c.prendasSugeridas.isEmpty())
        assertTrue(c.mensaje.contains("paraguas"))
    }

    @Test
    fun `con frio sugiere abrigo`() {
        val c = caso(Pronostico(5.0, 14.0, 10), listOf(abrigo("campera negra")))
        assertEquals("Mañana hace frío", c.titulo)
    }

    @Test
    fun `con calor no sugiere abrigo`() {
        val c = caso(Pronostico(20.0, 32.0, 0), listOf(abrigo("campera negra")))
        assertEquals("Mañana hace calor", c.titulo)
        assertTrue(c.prendasSugeridas.isEmpty())
    }

    @Test
    fun `lluvia gana sobre frio`() {
        val c = caso(Pronostico(3.0, 10.0, 90), listOf(abrigo("campera negra")))
        assertEquals("Mañana llueve", c.titulo)
    }

    @Test
    fun `clima agradable no sugiere nada`() {
        val c = caso(Pronostico(15.0, 22.0, 10), listOf(abrigo("campera negra")))
        assertEquals("Mañana, clima agradable", c.titulo)
    }
}