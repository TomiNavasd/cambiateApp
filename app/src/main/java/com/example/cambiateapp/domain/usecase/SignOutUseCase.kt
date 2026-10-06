package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.cerrarSesion()
}