package com.example.cambiateapp.presentation.settings

import com.example.cambiateapp.domain.model.ModoTema

data class AjustesUiState(
    val modoTema: ModoTema = ModoTema.SISTEMA,
    val aviso: String? = null,
)