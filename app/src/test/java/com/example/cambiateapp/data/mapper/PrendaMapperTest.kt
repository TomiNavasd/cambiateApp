package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.PrendaDto
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PrendaMapperTest {

    private fun dto(categoria: String = "superior", ocasiones: List<String> = listOf("casual")) =
        PrendaDto(
            id = "p1",
            userId = "u1",
            nombre = "Remera",
            categoria = categoria,
            colores = listOf("blanco"),
            ocasiones = ocasiones,
            fotoUrl = "http://x/y.jpg"
        )

    @Test
    fun `dto valido se convierte a dominio`() {
        val prenda = dto(ocasiones = listOf("casual", "formal")).toDomain()
        assertNotNull(prenda)
        assertEquals("p1", prenda!!.id)
        assertEquals(Categoria.SUPERIOR, prenda.categoria)
        assertEquals(listOf(Ocasion.CASUAL, Ocasion.FORMAL), prenda.ocasiones)
    }

    @Test
    fun `categoria desconocida devuelve null`() {
        assertNull(dto(categoria = "pepe").toDomain())
    }

    @Test
    fun `ocasiones desconocidas se ignoran`() {
        val prenda = dto(ocasiones = listOf("casual", "marciano")).toDomain()
        assertEquals(listOf(Ocasion.CASUAL), prenda!!.ocasiones)
    }

    @Test
    fun `dominio a dto usa minusculas y agrega el userId`() {
        val prenda = Prenda(
            id = "",
            nombre = "Jean",
            descripcion = "",
            categoria = Categoria.INFERIOR,
            colores = listOf("azul"),
            ocasiones = listOf(Ocasion.CASUAL),
            fotoUrl = "http://x/y.jpg"
        )
        val dto = prenda.toDto("uid-123")
        assertEquals("uid-123", dto.userId)
        assertEquals("inferior", dto.categoria)
        assertEquals(listOf("casual"), dto.ocasiones)
    }
}