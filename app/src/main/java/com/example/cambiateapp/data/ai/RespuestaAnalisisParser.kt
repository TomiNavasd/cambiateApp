package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.model.AnalisisPrenda
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object RespuestaAnalisisParser {

    fun parsear(texto: String): AnalisisPrenda? {
        val inicio = texto.indexOf('{')
        val fin = texto.lastIndexOf('}')
        if (inicio !in 0 until fin) return null

        val json = try {
            JSONObject(texto.substring(inicio, fin + 1))
        } catch (e: JSONException) {
            return null
        }

        return AnalisisPrenda(
            nombre = json.optString("nombre"),
            descripcion = json.optString("descripcion"),
            categoria = Categoria.entries.firstOrNull {
                it.name.equals(json.optString("categoria").trim(), ignoreCase = true)
            },
            colores = json.optJSONArray("colores").aListaDeTextos(),
            ocasiones = json.optJSONArray("ocasiones").aListaDeTextos().mapNotNull { texto ->
                Ocasion.entries.firstOrNull { it.name.equals(texto.trim(), ignoreCase = true) }
            },
            etiquetas = json.optJSONArray("etiquetas").aListaDeTextos()
        )
    }

    private fun JSONArray?.aListaDeTextos(): List<String> =
        if (this == null) emptyList() else List(length()) { optString(it) }.filter { it.isNotBlank() }
}