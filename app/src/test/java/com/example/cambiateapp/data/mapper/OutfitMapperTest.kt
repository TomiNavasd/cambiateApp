package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.data.remote.OutfitDto
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OutfitMapperTest {

    private val outfit = Outfit(
        id = "o1",
        ocasion = Ocasion.TRABAJO,
        prendasIds = listOf("a", "b", "c"),
        fecha = Instant.ofEpochSecond(1_700_000_000L, 500),
        favorito = true
    )

    @Test
    fun `un outfit hace ida y vuelta sin perder datos`() {
        val vuelta = outfit.toDto(userId = "u1").toDomain()

        assertEquals(outfit, vuelta)
    }

    @Test
    fun `al guardar se registra el dueno y la ocasion en minuscula`() {
        val dto = outfit.toDto(userId = "u1")

        assertEquals("u1", dto.userId)
        assertEquals("trabajo", dto.ocasion)
        assertEquals(listOf("a", "b", "c"), dto.prendasIds)
        assertEquals(true, dto.favorito)
    }

    @Test
    fun `una ocasion desconocida devuelve null`() {
        val dto = OutfitDto(id = "x", userId = "u1", ocasion = "inexistente")

        assertNull(dto.toDomain())
    }

    @Test
    fun `una fecha ausente no rompe el mapeo`() {
        val dto = OutfitDto(id = "x", userId = "u1", ocasion = "casual", fecha = null)

        val resultado = dto.toDomain()

        assertNotNull(resultado)
        assertEquals(Instant.EPOCH, resultado!!.fecha)
    }
}