package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Prenda

sealed interface WardrobeUiState {
    data object Cargando : WardrobeUiState
    data object Vacio : WardrobeUiState
    data class Error(val mensaje: String) : WardrobeUiState
    data class Contenido(
        val prendas: List<Prenda>,
        val filtro: Categoria?,
        val consulta: String = ""
    ) : WardrobeUiState
}