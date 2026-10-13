package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudeOutfitSuggesterTest {

    /** Cliente HTTP falso: no sale a internet, responde lo que le indiquemos y registra los pedidos. */
    private class ClienteFalso(private val responder: (Request) -> Response) {
        var llamadas = 0
        var ultimaPeticion: Request? = null

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                llamadas++
                ultimaPeticion = chain.request()
                responder(chain.request())
            })
            .build()

        fun cuerpoEnviado(): String {
            val buffer = Buffer()
            ultimaPeticion!!.body!!.writeTo(buffer)
            return buffer.readUtf8()
        }
    }

    private fun http(pedido: Request, codigo: Int, cuerpo: String): Response =
        Response.Builder()
            .request(pedido)
            .protocol(Protocol.HTTP_1_1)
            .code(codigo)
            .message("mensaje")
            .body(cuerpo.toResponseBody("application/json".toMediaType()))
            .build()

    /** Arma una respuesta con el formato real de la API de Anthropic. */
    private fun respuestaClaude(texto: String): String =
        JSONObject()
            .put(
                "content",
                JSONArray().put(JSONObject().put("type", "text").put("text", texto))
            )
            .toString()

    private val remera = Prenda(
        "idFirestore1", "Remera blanca", "", Categoria.SUPERIOR,
        listOf("blanco"), listOf(Ocasion.CASUAL), "https://foto.test/remera.jpg"
    )
    private val jean = Prenda(
        "idFirestore2", "Jean azul", "", Categoria.INFERIOR,
        listOf("azul"), listOf(Ocasion.CASUAL), "https://foto.test/jean.jpg"
    )
    private val zapatillas = Prenda(
        "idFirestore3", "Zapatillas negras", "", Categoria.CALZADO,
        listOf("negro"), listOf(Ocasion.CASUAL), "https://foto.test/zapas.jpg"
    )
    private val traje = Prenda(
        "idFirestore4", "Traje formal", "", Categoria.SUPERIOR,
        listOf("gris"), listOf(Ocasion.TRABAJO), "https://foto.test/traje.jpg"
    )
    private val prendas = listOf(remera, jean, zapatillas, traje)

    private val respuestaValida = respuestaClaude("""{"outfits":[{"prendasIds":["p1","p2","p3"]}]}""")

    @Test
    fun `devuelve los outfits de Claude con los ids reales de las prendas`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaValida) }

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        val outfits = resultado.getOrThrow()
        assertEquals(1, outfits.size)
        assertEquals(listOf("idFirestore1", "idFirestore2", "idFirestore3"), outfits[0].prendasIds)
        assertTrue(outfits[0].esNuevo)
    }

    @Test
    fun `el pedido lleva la clave y la version de la API`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaValida) }

        ClaudeOutfitSuggester("mi-clave-de-prueba", falso.client).sugerir(Ocasion.CASUAL, prendas)

        val pedido = falso.ultimaPeticion!!
        assertEquals("POST", pedido.method)
        assertEquals("https://api.anthropic.com/v1/messages", pedido.url.toString())
        assertEquals("mi-clave-de-prueba", pedido.header("x-api-key"))
        assertEquals("2023-06-01", pedido.header("anthropic-version"))
    }

    @Test
    fun `el pedido no filtra ids, fotos ni prendas de otras ocasiones`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaValida) }

        ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        val cuerpo = falso.cuerpoEnviado()
        assertTrue(cuerpo.contains("p1"))               // viajan alias cortos
        assertTrue(cuerpo.contains("Remera blanca"))    // nombre y colores sí
        assertFalse(cuerpo.contains("idFirestore"))     // ids reales no
        assertFalse(cuerpo.contains("foto.test"))       // fotos no
        assertFalse(cuerpo.contains("Traje formal"))    // prenda de otra ocasión no
    }

    @Test
    fun `una clave invalida (401) devuelve SinPermisos`() = runBlocking {
        val falso = ClienteFalso { http(it, 401, """{"error":"invalid x-api-key"}""") }

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        assertEquals(DomainException.SinPermisos, resultado.exceptionOrNull())
    }

    @Test
    fun `el limite de uso (429) devuelve un error desconocido`() = runBlocking {
        val falso = ClienteFalso { http(it, 429, """{"error":"rate limit"}""") }

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        assertTrue(resultado.exceptionOrNull() is DomainException.Desconocido)
    }

    @Test
    fun `sin internet devuelve SinConexion`() = runBlocking {
        val falso = ClienteFalso { throw IOException("sin red") }

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        assertEquals(DomainException.SinConexion, resultado.exceptionOrNull())
    }

    @Test
    fun `una respuesta que no es JSON devuelve un error desconocido`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaClaude("Lo siento, no puedo ayudarte")) }

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, prendas)

        assertTrue(resultado.exceptionOrNull() is DomainException.Desconocido)
    }

    @Test
    fun `sin clave no se hace ninguna llamada`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaValida) }

        val resultado = ClaudeOutfitSuggester("", falso.client).sugerir(Ocasion.CASUAL, prendas)

        assertEquals(DomainException.SinPermisos, resultado.exceptionOrNull())
        assertEquals(0, falso.llamadas)
    }

    @Test
    fun `si faltan categorias no se hace ninguna llamada`() = runBlocking {
        val falso = ClienteFalso { http(it, 200, respuestaValida) }
        val sinCalzado = listOf(remera, jean)

        val resultado = ClaudeOutfitSuggester("clave", falso.client).sugerir(Ocasion.CASUAL, sinCalzado)

        assertTrue(resultado.getOrThrow().isEmpty())
        assertEquals(0, falso.llamadas)
    }
}