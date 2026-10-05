package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val usuarioActual: Flow<User?>
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun registrar(email: String, pass: String): Result<User>
    fun cerrarSesion()
}