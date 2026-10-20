package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Categoria

/** Mira la foto de una prenda y devuelve etiquetas que la describen. */
interface AnalizadorDePrenda {
    suspend fun analizar(fotoUrl: String, categoria: Categoria): Result<List<String>>
}