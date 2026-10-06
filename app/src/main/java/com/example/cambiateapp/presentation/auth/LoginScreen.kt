package com.example.cambiateapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.presentation.components.AppLogo
import com.example.cambiateapp.presentation.components.AppTextField
import com.example.cambiateapp.presentation.components.PasswordTextField
import com.example.cambiateapp.presentation.components.PrimaryButton
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel con la pantalla y avisa cuando el login salió bien. */
@Composable
fun LoginRoute(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.estado) {
        if (state.estado is AuthEstado.Exito) onLoginExitoso()
    }

    LoginScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onIrARegistro = onIrARegistro
    )
}

/** Pantalla "tonta": solo dibuja el estado y manda eventos hacia arriba. */
@Composable
fun LoginScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val estado = state.estado

    // Efecto de una sola vez: muestra el error general y avisa al ViewModel que ya se mostró.
    LaunchedEffect(estado) {
        if (estado is AuthEstado.Error) {
            snackbarHostState.showSnackbar(estado.mensaje)
            onEvent(AuthUiEvent.ErrorMostrado)
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
                text = "CambiateApp",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tu estilo, siempre cerca",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            AppTextField(
                value = state.email,
                onValueChange = { onEvent(AuthUiEvent.EmailCambio(it)) },
                label = "Email",
                leadingIcon = Icons.Rounded.Email,
                error = state.emailError,
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(8.dp))
            PasswordTextField(
                value = state.password,
                onValueChange = { onEvent(AuthUiEvent.PasswordCambio(it)) },
                label = "Contraseña",
                error = state.passwordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { onEvent(AuthUiEvent.LoginClick) })
            )
            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                texto = "Iniciar sesión",
                onClick = { onEvent(AuthUiEvent.LoginClick) },
                cargando = state.estaCargando
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿No tenés una cuenta?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onIrARegistro) {
                    Text(text = "Registrate", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    CambiateAppTheme {
        LoginScreen(
            state = AuthUiState(email = "ana@ejemplo.com", emailError = "Ingresá un email válido"),
            onEvent = {},
            onIrARegistro = {}
        )
    }
}