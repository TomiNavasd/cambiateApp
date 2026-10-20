package com.example.cambiateapp.presentation.auth

sealed interface AuthEstado {
    data object Inactivo : AuthEstado
    data object Cargando : AuthEstado
    data object Exito : AuthEstado
    data class Error(val mensaje: String) : AuthEstado
}

data class RecuperacionUiState(
    val abierta: Boolean = false,
    val email: String = "",
    val error: String? = null,
    val enviando: Boolean = false
)

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val estado: AuthEstado = AuthEstado.Inactivo,
    val recuperacion: RecuperacionUiState = RecuperacionUiState(),
    val mensaje: String? = null
) {
    val estaCargando: Boolean get() = estado is AuthEstado.Cargando
}