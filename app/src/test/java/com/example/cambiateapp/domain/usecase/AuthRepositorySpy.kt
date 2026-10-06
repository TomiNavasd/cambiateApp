package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.User
import com.example.cambiateapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AuthRepositorySpy : AuthRepository {

    var llamadasLogin = 0
    var llamadasRegistrar = 0
    var ultimoEmail: String? = null

    override val usuarioActual: Flow<User?> = flowOf(null)

    override suspend fun login(email: String, pass: String): Result<User> {
        llamadasLogin++
        ultimoEmail = email
        return Result.success(User(id = "uid-prueba", email = email))
    }

    override suspend fun registrar(email: String, pass: String): Result<User> {
        llamadasRegistrar++
        ultimoEmail = email
        return Result.success(User(id = "uid-prueba", email = email))
    }

    override fun cerrarSesion() {}
}