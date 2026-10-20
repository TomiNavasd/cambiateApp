package com.example.cambiateapp.data.ai

import org.json.JSONException
import org.json.JSONObject

internal object RespuestaEtiquetasParser {

    /** Saca la lista de etiquetas del texto de Claude. Si no entiende la respuesta, devuelve vacío. */
    fun parsear(texto: String): List<String> {
        val inicio = texto.indexOf('{')
        val fin = texto.lastIndexOf('}')
        if (inicio !in 0 until fin) return emptyList()

        val etiquetas = try {
            JSONObject(texto.substring(inicio, fin + 1)).optJSONArray("etiquetas")
        } catch (e: JSONException) {
            null
        } ?: return emptyList()

        return List(etiquetas.length()) { etiquetas.optString(it) }
            .filter { it.isNotBlank() }
    }
}