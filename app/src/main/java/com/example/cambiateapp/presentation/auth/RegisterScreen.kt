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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RegisterRoute(
    onRegistroExitoso: () -> Unit,
    onVolver: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.estado) {
        if (state.estado is AuthEstado.Exito) onRegistroExitoso()
    }

    RegisterScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    onVolver: () -> Unit,
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

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.registro_titulo),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.accion_volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AppLogo(size = 72.dp)
            Spacer(Modifier.height(24.dp))

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
                ayuda = stringResource(R.string.registro_password_ayuda)
            )
            Spacer(Modifier.height(8.dp))
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = { onEvent(AuthUiEvent.ConfirmPasswordCambio(it)) },
                label = stringResource(R.string.registro_confirmar),
                error = state.confirmPasswordError,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { onEvent(AuthUiEvent.RegistroClick) })
            )
            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                texto = stringResource(R.string.registro_crear),
                onClick = { onEvent(AuthUiEvent.RegistroClick) },
                cargando = state.estaCargando
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.registro_con_cuenta),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onVolver) {
                    Text(
                        text = stringResource(R.string.registro_inicia_sesion),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    CambiateAppTheme {
        RegisterScreen(
            state = AuthUiState(
                email = "ana@ejemplo.com",
                confirmPasswordError = "Las contraseñas no coinciden"
            ),
            onEvent = {},
            onVolver = {}
        )
    }
}