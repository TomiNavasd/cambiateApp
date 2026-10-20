package com.example.cambiateapp.presentation.auth

sealed interface AuthUiEvent {
    data class EmailCambio(val valor: String) : AuthUiEvent
    data class PasswordCambio(val valor: String) : AuthUiEvent
    data class ConfirmPasswordCambio(val valor: String) : AuthUiEvent
    data object LoginClick : AuthUiEvent
    data object RegistroClick : AuthUiEvent
    data object ErrorMostrado : AuthUiEvent

    // Recuperar contraseña
    data object RecuperarClick : AuthUiEvent
    data class RecuperarEmailCambio(val valor: String) : AuthUiEvent
    data object RecuperarEnviarClick : AuthUiEvent
    data object RecuperarCancelar : AuthUiEvent
    data object MensajeMostrado : AuthUiEvent
}