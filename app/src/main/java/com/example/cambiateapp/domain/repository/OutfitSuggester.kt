package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda

interface OutfitSuggester {
    suspend fun sugerir(ocasion: Ocasion, prendas: List<Prenda>): Result<List<Outfit>>
}