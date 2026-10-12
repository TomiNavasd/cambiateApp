package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion

sealed interface AltaEdicionUiEvent {
    data class NombreCambio(val valor: String) : AltaEdicionUiEvent
    data class DescripcionCambio(val valor: String) : AltaEdicionUiEvent
    data class CategoriaSeleccionada(val categoria: Categoria) : AltaEdicionUiEvent
    data class ColoresCambio(val valor: String) : AltaEdicionUiEvent
    data class OcasionAlternada(val ocasion: Ocasion) : AltaEdicionUiEvent
    data class FotoUrlCambio(val valor: String) : AltaEdicionUiEvent
    data object Guardar : AltaEdicionUiEvent
    data object ReintentarCarga : AltaEdicionUiEvent
}