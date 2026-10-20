package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.repository.AuthRepository
import javax.inject.Inject

class EnviarRecuperacionContrasenaUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val emailLimpio = email.trim()
        if (!esEmailValido(emailLimpio)) {
            return Result.failure(DomainException.EmailInvalido)
        }
        return authRepository.enviarRecuperacionContrasena(emailLimpio)
    }
}