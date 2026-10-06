package com.example.cambiateapp.presentation.auth

// Estados excluyentes de la operación: no se puede estar cargando y con error a la vez.
sealed interface AuthEstado {
    data object Inactivo : AuthEstado
    data object Cargando : AuthEstado
    data object Exito : AuthEstado
    data class Error(val mensaje: String) : AuthEstado
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val estado: AuthEstado = AuthEstado.Inactivo
) {
    val estaCargando: Boolean get() = estado is AuthEstado.Cargando
}