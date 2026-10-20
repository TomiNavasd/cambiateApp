package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.User
import com.example.cambiateapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginConGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        if (idToken.isBlank()) {
            return Result.failure(DomainException.CredencialesInvalidas)
        }
        return authRepository.loginConGoogle(idToken)
    }
}