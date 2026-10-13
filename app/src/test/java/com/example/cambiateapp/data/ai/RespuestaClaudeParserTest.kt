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
    fun `devuelve como maximo tres outfits`() {
        val outfit = """{"prendasIds":["s1","i1","c1"]}"""
        val distintos = """{"prendasIds":["s2","i1","c1"]}"""
        val conAbrigo1 = """{"prendasIds":["s1","i1","c1","a1"]}"""
        val conAbrigo2 = """{"prendasIds":["s2","i1","c1","a1"]}"""

        val resultado = parsear("""{"outfits":[$outfit,$distintos,$conAbrigo1,$conAbrigo2]}""")

        assertEquals(3, resultado.size)
    }
}