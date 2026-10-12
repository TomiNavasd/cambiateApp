package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion

data class AltaEdicionUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,          // trayendo la prenda a editar
    val noEncontrada: Boolean = false,
    val errorCarga: String? = null,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,

    val nombre: String = "",
    val descripcion: String = "",
    val categoria: Categoria = Categoria.SUPERIOR,
    val colores: String = "",               // texto crudo, separado por comas
    val ocasiones: Set<Ocasion> = emptySet(),
    val fotoUrl: String = "",

    val errorNombre: String? = null,
    val errorFoto: String? = null,
    val errorGeneral: String? = null
)