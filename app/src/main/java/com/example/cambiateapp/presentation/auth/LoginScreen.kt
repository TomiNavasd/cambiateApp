package com.example.cambiateapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.R
import com.example.cambiateapp.presentation.components.AppLogo
import com.example.cambiateapp.presentation.components.AppTextField
import com.example.cambiateapp.presentation.components.PasswordTextField
import com.example.cambiateapp.presentation.components.PrimaryButton
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Lo genera el plugin de google-services a partir de google-services.json.
    val webClientId = stringResource(R.string.default_web_client_id)

    LaunchedEffect(state.estado) {
        if (state.estado is AuthEstado.Exito) onLoginExitoso()
    }

    LoginScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onIrARegistro = onIrARegistro,
        onGoogleClick = {
            if (!state.estaCargando) {
                viewModel.onEvent(AuthUiEvent.GoogleIniciado)
                scope.launch {
                    when (val resultado = pedirTokenDeGoogle(context, webClientId)) {
                        is ResultadoGoogle.Token ->
                            viewModel.onEvent(AuthUiEvent.GoogleTokenObtenido(resultado.idToken))
                        ResultadoGoogle.Cancelado ->
                            viewModel.onEvent(AuthUiEvent.GoogleCancelado)
                        is ResultadoGoogle.Error ->
                            viewModel.onEvent(AuthUiEvent.GoogleFallo(resultado.mensaje))
                    }
                }
            }
        }
    )
}

/** Pantalla "tonta": solo dibuja el estado y manda eventos hacia arriba. */
@Composable
fun LoginScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    onIrARegistro: () -> Unit,
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val estado = state.estado

    LaunchedEffect(estado) {
        if (estado is AuthEstado.Error) {
            snackbarHostState.showSnackbar(estado.mensaje)
            onEvent(AuthUiEvent.ErrorMostrado)
        }
    }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(AuthUiEvent.MensajeMostrado)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppLogo()
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.auth_slogan),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onEvent(AuthUiEvent.EmailCambio(it)) },
                label = stringResource(R.string.auth_email),
                leadingIcon = Icons.Rounded.Email,
                error = state.emailError,
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(8.dp))
            PasswordTextField(
                value = state.password,
                onValueChange = { onEvent(AuthUiEvent.PasswordCambio(it)) },
                label = stringResource(R.string.auth_password),
                error = state.passwordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { onEvent(AuthUiEvent.LoginClick) })
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEvent(AuthUiEvent.RecuperarClick) }) {
                    Text(
                        text = stringResource(R.string.auth_olvidaste_password),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                texto = stringResource(R.string.auth_iniciar_sesion),
                onClick = { onEvent(AuthUiEvent.LoginClick) },
                cargando = state.estaCargando
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_o),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !state.estaCargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_continuar_google),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.auth_sin_cuenta),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onIrARegistro) {
                    Text(
                        text = stringResource(R.string.auth_registrate),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (state.recuperacion.abierta) {
        RecuperarContrasenaDialog(
            state = state.recuperacion,
            onEvent = onEvent
        )
    }
}

/** Diálogo para pedir el mail al que se manda el link de recuperación. */
@Composable
private fun RecuperarContrasenaDialog(
    state: RecuperacionUiState,
    onEvent: (AuthUiEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AuthUiEvent.RecuperarCancelar) },
        title = { Text(stringResource(R.string.recuperar_titulo)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.recuperar_descripcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value = state.email,
                    onValueChange = { onEvent(AuthUiEvent.RecuperarEmailCambio(it)) },
                    label = stringResource(R.string.auth_email),
                    leadingIcon = Icons.Rounded.Email,
                    error = state.error,
                    keyboardType = KeyboardType.Email
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(AuthUiEvent.RecuperarEnviarClick) },
                enabled = !state.enviando
            ) {
                Text(
                    stringResource(
                        if (state.enviando) R.string.recuperar_enviando else R.string.recuperar_enviar
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(AuthUiEvent.RecuperarCancelar) },
                enabled = !state.enviando
            ) {
                Text(stringResource(R.string.recuperar_cancelar))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    CambiateAppTheme {
        LoginScreen(
            state = AuthUiState(email = "ana@ejemplo.com", emailError = "Ingresá un email válido"),
            onEvent = {},
            onIrARegistro = {},
            onGoogleClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenRecuperacionPreview() {
    CambiateAppTheme {
        LoginScreen(
            state = AuthUiState(
                email = "ana@ejemplo.com",
                recuperacion = RecuperacionUiState(abierta = true, email = "ana@ejemplo.com")
            ),
            onEvent = {},
            onIrARegistro = {},
            onGoogleClick = {}
        )
    }
}