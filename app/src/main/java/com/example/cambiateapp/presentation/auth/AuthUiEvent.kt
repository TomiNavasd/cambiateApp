package com.example.cambiateapp.presentation.auth

sealed interface AuthUiEvent {
    data class EmailCambio(val valor: String) : AuthUiEvent
    data class PasswordCambio(val valor: String) : AuthUiEvent
    data class ConfirmPasswordCambio(val valor: String) : AuthUiEvent
    data object LoginClick : AuthUiEvent
    data object RegistroClick : AuthUiEvent
    data object ErrorMostrado : AuthUiEvent
}