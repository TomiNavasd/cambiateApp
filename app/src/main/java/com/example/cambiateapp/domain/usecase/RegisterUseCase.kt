package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.User
import com.example.cambiateapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        val emailLimpio = email.trim()
        if (!esEmailValido(emailLimpio)) {
            return Result.failure(DomainException.EmailInvalido)
        }
        if (pass.length < LARGO_MINIMO_CONTRASENA) {
            return Result.failure(DomainException.ContrasenaDebil)
        }
        return authRepository.registrar(emailLimpio, pass)
    }

    private companion object {
        const val LARGO_MINIMO_CONTRASENA = 6
    }
}