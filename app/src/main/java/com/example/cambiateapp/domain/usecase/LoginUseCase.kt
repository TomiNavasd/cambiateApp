package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.User
import com.example.cambiateapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        val emailLimpio = email.trim()
        if (!esEmailValido(emailLimpio)) {
            return Result.failure(DomainException.EmailInvalido)
        }
        if (pass.isEmpty()) {
            return Result.failure(DomainException.Validacion(CampoInvalido.CONTRASENA))
        }
        return authRepository.login(emailLimpio, pass)
    }
}