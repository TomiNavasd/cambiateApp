package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasOutfitSuggesterTest {

    private val sugeridor = ReglasOutfitSuggester(Random(42))

    private fun prenda(id: String, categoria: Categoria, vararg ocasiones: Ocasion) = Prenda(
        id = id,
        nombre = "Prenda $id",
        descripcion = "",
        categoria = categoria,
        colores = listOf("negro"),
        ocasiones = ocasiones.toList(),
        fotoUrl = "http://foto/$id"
    )

    @Test
    fun `si falta una categoria basica devuelve lista vacia`() {
        runBlocking {
            val prendas = listOf(
                prenda("1", Categoria.SUPERIOR, Ocasion.TRABAJO),
                prenda("2", Categoria.INFERIOR, Ocasion.TRABAJO)
            )

            val resultado = sugeridor.sugerir(Ocasion.TRABAJO, prendas)

            assertTrue(resultado.isSuccess)
            assertTrue(resultado.getOrThrow().isEmpty())
        }
    }

    @Test
    fun `solo usa prendas de la ocasion pedida`() {
        runBlocking {
            val prendas = listOf(
                prenda("s-trabajo", Categoria.SUPERIOR, Ocasion.TRABAJO),
                prenda("i-trabajo", Categoria.INFERIOR, Ocasion.TRABAJO),
                prenda("c-trabajo", Categoria.CALZADO, Ocasion.TRABAJO),
                prenda("s-deporte", Categoria.SUPERIOR, Ocasion.DEPORTE),
                prenda("i-deporte", Categoria.INFERIOR, Ocasion.DEPORTE),
                prenda("c-deporte", Categoria.CALZADO, Ocasion.DEPORTE)
            )

            val outfits = sugeridor.sugerir(Ocasion.TRABAJO, prendas).getOrThrow()

            assertEquals(1, outfits.size)
            assertEquals(
                setOf("s-trabajo", "i-trabajo", "c-trabajo"),
                outfits.first().prendasIds.toSet()
            )
        }
    }

    @Test
    fun `cada outfit lleva una prenda superior, una inferior y un calzado`() {
        runBlocking {
            val prendas = listOf(
                prenda("s1", Categoria.SUPERIOR, Ocasion.CASUAL),
                prenda("s2", Categoria.SUPERIOR, Ocasion.CASUAL),
                prenda("i1", Categoria.INFERIOR, Ocasion.CASUAL),
                prenda("i2", Categoria.INFERIOR, Ocasion.CASUAL),
                prenda("c1", Categoria.CALZADO, Ocasion.CASUAL)
            )
            val porId = prendas.associateBy { it.id }

            val outfits = sugeridor.sugerir(Ocasion.CASUAL, prendas).getOrThrow()

            assertTrue(outfits.isNotEmpty())
            outfits.forEach { outfit ->
                val categorias = outfit.prendasIds.map { porId.getValue(it).categoria }
                assertEquals(1, categorias.count { it == Categoria.SUPERIOR })
                assertEquals(1, categorias.count { it == Categoria.INFERIOR })
                assertEquals(1, categorias.count { it == Categoria.CALZADO })
            }
        }
    }

    @Test
    fun `devuelve como maximo tres outfits y no se repiten`() {
        runBlocking {
            val prendas = listOf("1", "2", "3").flatMap { n ->
                listOf(
                    prenda("s$n", Categoria.SUPERIOR, Ocasion.SALIDA),
                    prenda("i$n", Categoria.INFERIOR, Ocasion.SALIDA),
                    prenda("c$n", Categoria.CALZADO, Ocasion.SALIDA)
                )
            }

            val outfits = sugeridor.sugerir(Ocasion.SALIDA, prendas).getOrThrow()

            assertEquals(3, outfits.size)
            assertEquals(3, outfits.map { it.prendasIds.toSet() }.distinct().size)
        }
    }

    @Test
    fun `suma un abrigo cuando hay uno para la ocasion`() {
        runBlocking {
            val prendas = listOf(
                prenda("s1", Categoria.SUPERIOR, Ocasion.CASAMIENTO),
                prenda("i1", Categoria.INFERIOR, Ocasion.CASAMIENTO),
                prenda("c1", Categoria.CALZADO, Ocasion.CASAMIENTO),
                prenda("a1", Categoria.ABRIGO, Ocasion.CASAMIENTO)
            )

            val outfit = sugeridor.sugerir(Ocasion.CASAMIENTO, prendas).getOrThrow().first()

            assertTrue("a1" in outfit.prendasIds)
        }
    }

    @Test
    fun `los outfits sugeridos todavia no estan guardados`() {
        runBlocking {
            val prendas = listOf(
                prenda("s1", Categoria.SUPERIOR, Ocasion.CASUAL),
                prenda("i1", Categoria.INFERIOR, Ocasion.CASUAL),
                prenda("c1", Categoria.CALZADO, Ocasion.CASUAL)
            )

            val outfit = sugeridor.sugerir(Ocasion.CASUAL, prendas).getOrThrow().first()

            assertTrue(outfit.esNuevo)
        }
    }
}