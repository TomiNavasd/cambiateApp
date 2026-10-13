package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import java.time.Instant
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object RespuestaClaudeParser {

    private const val MAX_OUTFITS = 3

    fun parsear(
        texto: String,
        ocasion: Ocasion,
        categoriaPorId: Map<String, Categoria>,
        ahora: Instant = Instant.now()
    ): List<Outfit> {
        val json = extraerJson(texto) ?: return emptyList()
        val outfitsJson = try {
            JSONObject(json).optJSONArray("outfits")
        } catch (e: JSONException) {
            null
        } ?: return emptyList()

        val vistos = mutableSetOf<Set<String>>()
        val resultado = mutableListOf<Outfit>()

        for (i in 0 until outfitsJson.length()) {
            val ids = outfitsJson.optJSONObject(i)
                ?.optJSONArray("prendasIds")
                ?.aListaDeTextos()
                .orEmpty()
                .filter { it in categoriaPorId } // descarta ids inventados
                .distinct()

            val categorias = ids.mapNotNull { categoriaPorId[it] }.toSet()
            val completo = Categoria.SUPERIOR in categorias &&
                    Categoria.INFERIOR in categorias &&
                    Categoria.CALZADO in categorias

            if (!completo || !vistos.add(ids.toSet())) continue

            resultado += Outfit(
                id = "",
                ocasion = ocasion,
                prendasIds = ids,
                fecha = ahora,
                favorito = false
            )
            if (resultado.size == MAX_OUTFITS) break
        }
        return resultado
    }

    private fun extraerJson(texto: String): String? {
        val inicio = texto.indexOf('{')
        val fin = texto.lastIndexOf('}')
        return if (inicio in 0 until fin) texto.substring(inicio, fin + 1) else null
    }

    private fun JSONArray.aListaDeTextos(): List<String> = List(length()) { optString(it) }
}