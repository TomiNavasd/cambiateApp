package com.example.cambiateapp.presentation.wardrobe

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion

data class AltaEdicionUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,          // trayendo la prenda a editar
    val noEncontrada: Boolean = false,
    val errorCarga: String? = null,
    val guardando: Boolean = false,         // subiendo la foto y/o guardando
    val guardadoExitoso: Boolean = false,

    val nombre: String = "",
    val descripcion: String = "",
    val categoria: Categoria = Categoria.SUPERIOR,
    val colores: String = "",               // texto crudo, separado por comas
    val ocasiones: Set<Ocasion> = emptySet(),
    val fotoUrl: String = "",               // URL ya subida (la que se guarda en la prenda)
    val fotoLocalUri: String? = null,       // foto recién elegida, todavía sin subir

    val errorNombre: String? = null,
    val errorFoto: String? = null,
    val errorGeneral: String? = null
) {
    /** Lo que se muestra en pantalla: la foto nueva si hay, si no la que ya tenía la prenda. */
    val fotoMostrada: String get() = fotoLocalUri ?: fotoUrl
}