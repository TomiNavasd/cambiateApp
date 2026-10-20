package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.ModoTema
import kotlinx.coroutines.flow.Flow

interface TemaRepository {

    /** Emite el modo elegido, y vuelve a emitir cuando el usuario lo cambia. */
    fun observarModo(): Flow<ModoTema>

    suspend fun guardarModo(modo: ModoTema)
}