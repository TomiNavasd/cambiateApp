package com.example.cambiateapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConectividadRepository {
    val hayConexion: Flow<Boolean>
}