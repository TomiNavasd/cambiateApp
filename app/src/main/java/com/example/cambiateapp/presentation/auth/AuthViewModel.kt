package com.example.cambiateapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.usecase.LoginUseCase
import com.example.cambiateapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailCambio ->
                _uiState.update { it.copy(email = event.valor, emailError = null) }

            is AuthUiEvent.PasswordCambio ->
                _uiState.update { it.copy(password = event.valor, passwordError = null) }

            is AuthUiEvent.ConfirmPasswordCambio ->
                _uiState.update {
                    it.copy(confirmPassword = event.valor, confirmPasswordError = null)
                }

            AuthUiEvent.LoginClick -> login()
            AuthUiEvent.RegistroClick -> registrar()

            AuthUiEvent.ErrorMostrado ->
                _uiState.update {
                    if (it.estado is AuthEstado.Error) it.copy(estado = AuthEstado.Inactivo) else it
                }
        }
    }

    private fun login() {
        val actual = _uiState.value
        if (actual.estaCargando) return
        _uiState.update { it.copy(estado = AuthEstado.Cargando) }
        viewModelScope.launch {
            loginUseCase(actual.email, actual.password)
                .onSuccess { _uiState.update { it.copy(estado = AuthEstado.Exito) } }
                .onFailure { error -> _uiState.update { aplicarError(it, error) } }
        }
    }

    private fun registrar() {
        val actual = _uiState.value
        if (actual.estaCargando) return
        if (actual.password != actual.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden") }
            return
        }
        _uiState.update { it.copy(estado = AuthEstado.Cargando) }
        viewModelScope.launch {
            registerUseCase(actual.email, actual.password)
                .onSuccess { _uiState.update { it.copy(estado = AuthEstado.Exito) } }
                .onFailure { error -> _uiState.update { aplicarError(it, error) } }
        }
    }

    // Traduce los errores del dominio a mensajes para mostrar en pantalla.
    private fun aplicarError(state: AuthUiState, error: Throwable): AuthUiState {
        val inactivo = state.copy(estado = AuthEstado.Inactivo)
        return when (error) {
            DomainException.EmailInvalido ->
                inactivo.copy(emailError = "Ingresá un email válido")

            DomainException.ContrasenaDebil ->
                inactivo.copy(passwordError = "La contraseña debe tener al menos 6 caracteres")

            DomainException.EmailYaRegistrado ->
                inactivo.copy(emailError = "Ese email ya está registrado")

            is DomainException.Validacion ->
                if (error.campo == CampoInvalido.CONTRASENA) {
                    inactivo.copy(passwordError = "Ingresá tu contraseña")
                } else {
                    state.copy(estado = AuthEstado.Error("Revisá los datos ingresados"))
                }

            DomainException.CredencialesInvalidas ->
                state.copy(estado = AuthEstado.Error("Email o contraseña incorrectos"))

            DomainException.SinConexion ->
                state.copy(estado = AuthEstado.Error("Sin conexión. Revisá tu internet e intentá de nuevo"))

            else ->
                state.copy(estado = AuthEstado.Error("Ocurrió un error inesperado. Intentá de nuevo"))
        }
    }
}