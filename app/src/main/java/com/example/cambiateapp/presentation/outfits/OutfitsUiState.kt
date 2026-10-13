package com.example.cambiateapp.presentation.outfits

import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda

data class OutfitConPrendas(val outfit: Outfit, val prendas: List<Prenda>)

sealed interface OutfitsUiState {
    data object Cargando : OutfitsUiState
    data class Error(val mensaje: String) : OutfitsUiState
    data class Contenido(
        val ocasion: Ocasion,
        val sugerencias: List<OutfitConPrendas>,
        val guardados: List<OutfitConPrendas>,
        val sugiriendo: Boolean,
        val aviso: String?
    ) : OutfitsUiState
}