package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RespuestaClaudeParserTest {

    private fun prenda(id: String, categoria: Categoria) =
        Prenda(id, "Prenda $id", "", categoria, listOf("azul"), listOf(Ocasion.CASUAL), "")

    private val prendas = listOf(
        prenda("s1", Categoria.SUPERIOR),
        prenda("s2", Categoria.SUPERIOR),
        prenda("i1", Categoria.INFERIOR),
        prenda("c1", Categoria.CALZADO),
        prenda("a1", Categoria.ABRIGO)
    )

    private fun parsear(texto: String) =
        RespuestaClaudeParser.parsear(texto, Ocasion.CASUAL, prendas.associate { it.id to it.categoria })

    @Test
    fun `una respuesta valida devuelve el outfit con sus prendas`() {
        val resultado = parsear("""{"outfits":[{"prendasIds":["s1","i1","c1","a1"]}]}""")

        assertEquals(1, resultado.size)
        assertEquals(listOf("s1", "i1", "c1", "a1"), resultado[0].prendasIds)
        assertEquals(Ocasion.CASUAL, resultado[0].ocasion)
        assertTrue(resultado[0].esNuevo)
    }

    @Test
    fun `acepta el JSON envuelto en texto y bloque de codigo`() {
        val texto = "Acá tenés:\n```json\n{\"outfits\":[{\"prendasIds\":[\"s1\",\"i1\",\"c1\"]}]}\n```"

        assertEquals(1, parsear(texto).size)
    }

    @Test
    fun `descarta ids que no existen en el armario`() {
        val resultado = parsear("""{"outfits":[{"prendasIds":["s1","i1","c1","fantasma"]}]}""")

        assertEquals(listOf("s1", "i1", "c1"), resultado[0].prendasIds)
    }

    @Test
    fun `descarta outfits sin calzado`() {
        val resultado = parsear("""{"outfits":[{"prendasIds":["s1","i1"]}]}""")

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `un texto sin JSON devuelve lista vacia`() {
        assertTrue(parsear("Lo siento, no puedo ayudarte con eso").isEmpty())
    }

    @Test
    fun `no repite el mismo outfit`() {
        val resultado = parsear(
            """{"outfits":[{"prendasIds":["s1","i1","c1"]},{"prendasIds":["c1","s1","i1"]}]}"""
        )

        assertEquals(1, resultado.size)
    }

    @Test
    fun `devuelve como maximo diez outfits`() {
        // 4 superiores x 3 inferiores x 1 calzado = 12 outfits distintos y validos.
        val superiores = listOf("s1", "s2", "s3", "s4")
        val inferiores = listOf("i1", "i2", "i3")
        val armario = superiores.map { it to Categoria.SUPERIOR } +
                inferiores.map { it to Categoria.INFERIOR } +
                ("c1" to Categoria.CALZADO)

        val outfitsJson = superiores.flatMap { s ->
            inferiores.map { i -> """{"prendasIds":["$s","$i","c1"]}""" }
        }.joinToString(",")

        val resultado = RespuestaClaudeParser.parsear(
            """{"outfits":[$outfitsJson]}""",
            Ocasion.CASUAL,
            armario.toMap()
        )

        assertEquals(10, resultado.size)
    }
}