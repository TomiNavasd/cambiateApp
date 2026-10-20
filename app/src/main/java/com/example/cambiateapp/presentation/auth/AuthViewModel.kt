package com.example.cambiateapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.usecase.EnviarRecuperacionContrasenaUseCase
import com.example.cambiateapp.domain.usecase.LoginConGoogleUseCase
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
    private val registerUseCase: RegisterUseCase,
    private val enviarRecuperacionContrasena: EnviarRecuperacionContrasenaUseCase,
    private val loginConGoogleUseCase: LoginConGoogleUseCase
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

            AuthUiEvent.RecuperarClick -> abrirRecuperacion()

            is AuthUiEvent.RecuperarEmailCambio ->
                _uiState.update {
                    it.copy(recuperacion = it.recuperacion.copy(email = event.valor, error = null))
                }

            AuthUiEvent.RecuperarEnviarClick -> enviarRecuperacion()

            AuthUiEvent.RecuperarCancelar ->
                _uiState.update {
                    // Mientras se está enviando no dejamos cerrar, para no perder el resultado.
                    if (it.recuperacion.enviando) it else it.copy(recuperacion = RecuperacionUiState())
                }

            AuthUiEvent.MensajeMostrado ->
                _uiState.update { it.copy(mensaje = null) }

            // Mientras Google muestra su selector, la pantalla queda "cargando" para bloquear doble toque.
            AuthUiEvent.GoogleIniciado ->
                _uiState.update { if (it.estaCargando) it else it.copy(estado = AuthEstado.Cargando) }

            is AuthUiEvent.GoogleTokenObtenido -> loginConGoogle(event.idToken)

            // Cerró el selector: volvemos a Inactivo sin mostrar ningún error.
            AuthUiEvent.GoogleCancelado ->
                _uiState.update {
                    if (it.estado is AuthEstado.Cargando) it.copy(estado = AuthEstado.Inactivo) else it
                }

            is AuthUiEvent.GoogleFallo ->
                _uiState.update { it.copy(estado = AuthEstado.Error(event.mensaje)) }
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

    private fun loginConGoogle(idToken: String) {
        viewModelScope.launch {
            loginConGoogleUseCase(idToken)
                .onSuccess { _uiState.update { it.copy(estado = AuthEstado.Exito) } }
                .onFailure { error ->
                    _uiState.update { it.copy(estado = AuthEstado.Error(mensajeGoogle(error))) }
                }
        }
    }

    private fun mensajeGoogle(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.EmailYaRegistrado -> "Ese email ya está registrado con otro método de acceso"
        else -> "No pudimos iniciar sesión con Google. Intentá de nuevo"
    }

    private fun abrirRecuperacion() {
        _uiState.update {
            it.copy(recuperacion = RecuperacionUiState(abierta = true, email = it.email))
        }
    }

    private fun enviarRecuperacion() {
        val actual = _uiState.value.recuperacion
        if (actual.enviando) return
        _uiState.update { it.copy(recuperacion = it.recuperacion.copy(enviando = true, error = null)) }
        viewModelScope.launch {
            enviarRecuperacionContrasena(actual.email)
                .onSuccess {
                    // Cerramos el diálogo y avisamos por snackbar. El texto es el mismo
                    // exista o no la cuenta (ver AuthRepositoryImpl).
                    _uiState.update {
                        it.copy(
                            recuperacion = RecuperacionUiState(),
                            mensaje = "Si el mail está registrado, te enviamos un link para elegir una contraseña nueva"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            recuperacion = it.recuperacion.copy(
                                enviando = false,
                                error = mensajeRecuperacion(error)
                            )
                        )
                    }
                }
        }
    }

    private fun mensajeRecuperacion(error: Throwable): String = when (error) {
        DomainException.EmailInvalido -> "Ingresá un email válido"
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        else -> "No pudimos enviar el mail. Intentá de nuevo"
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