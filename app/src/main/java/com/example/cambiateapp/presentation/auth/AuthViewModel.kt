package com.example.cambiateapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.usecase.EnviarRecuperacionContrasenaUseCase
import com.example.cambiateapp.domain.usecase.LoginConGoogleUseCase
import com.example.cambiateapp.domain.usecase.LoginUseCase
import com.example.cambiateapp.domain.usecase.RegisterUseCase
import com.example.cambiateapp.presentation.common.ProveedorDeTextos
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
    private val loginConGoogleUseCase: LoginConGoogleUseCase,
    private val textos: ProveedorDeTextos
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
                    if (it.recuperacion.enviando) it else it.copy(recuperacion = RecuperacionUiState())
                }

            AuthUiEvent.MensajeMostrado ->
                _uiState.update { it.copy(mensaje = null) }

            AuthUiEvent.GoogleIniciado ->
                _uiState.update { if (it.estaCargando) it else it.copy(estado = AuthEstado.Cargando) }

            is AuthUiEvent.GoogleTokenObtenido -> loginConGoogle(event.idToken)

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
            _uiState.update {
                it.copy(confirmPasswordError = textos.obtener(R.string.auth_error_pass_no_coinciden))
            }
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

    private fun mensajeGoogle(error: Throwable): String = textos.obtener(
        when (error) {
            DomainException.SinConexion -> R.string.error_sin_conexion
            DomainException.EmailYaRegistrado -> R.string.google_error_email_otro_metodo
            else -> R.string.google_error_generico
        }
    )

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
                            mensaje = textos.obtener(R.string.recuperar_ok)
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

    private fun mensajeRecuperacion(error: Throwable): String = textos.obtener(
        when (error) {
            DomainException.EmailInvalido -> R.string.auth_error_email_invalido
            DomainException.SinConexion -> R.string.error_sin_conexion
            else -> R.string.recuperar_error_envio
        }
    )

    private fun aplicarError(state: AuthUiState, error: Throwable): AuthUiState {
        val inactivo = state.copy(estado = AuthEstado.Inactivo)
        return when (error) {
            DomainException.EmailInvalido ->
                inactivo.copy(emailError = textos.obtener(R.string.auth_error_email_invalido))

            DomainException.ContrasenaDebil ->
                inactivo.copy(passwordError = textos.obtener(R.string.auth_error_pass_debil))

            DomainException.EmailYaRegistrado ->
                inactivo.copy(emailError = textos.obtener(R.string.auth_error_email_registrado))

            is DomainException.Validacion ->
                if (error.campo == CampoInvalido.CONTRASENA) {
                    inactivo.copy(passwordError = textos.obtener(R.string.auth_error_pass_vacia))
                } else {
                    state.copy(estado = AuthEstado.Error(textos.obtener(R.string.error_revisar_datos)))
                }

            DomainException.CredencialesInvalidas ->
                state.copy(estado = AuthEstado.Error(textos.obtener(R.string.auth_error_credenciales)))

            DomainException.SinConexion ->
                state.copy(estado = AuthEstado.Error(textos.obtener(R.string.error_sin_conexion)))

            else ->
                state.copy(estado = AuthEstado.Error(textos.obtener(R.string.error_inesperado)))
        }
    }
}