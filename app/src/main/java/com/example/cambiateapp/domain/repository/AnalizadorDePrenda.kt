package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.AnalisisPrenda

interface AnalizadorDePrenda {
    suspend fun analizar(fotoUrl: String): Result<AnalisisPrenda>
}