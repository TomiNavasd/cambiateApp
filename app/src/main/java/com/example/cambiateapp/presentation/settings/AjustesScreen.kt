package com.example.cambiateapp.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel con la pantalla. No navega: avisa con callbacks. */
@Composable
fun AjustesRoute(
    onVolver: () -> Unit,
    onNotificacionesClick: () -> Unit,
    viewModel: AjustesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AjustesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNotificacionesClick = onNotificacionesClick,
        onVolver = onVolver
    )
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    state: AjustesUiState,
    onEvent: (AjustesUiEvent) -> Unit,
    onNotificacionesClick: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.aviso) {
        state.aviso?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(AjustesUiEvent.AvisoMostrado)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Apariencia", style = MaterialTheme.typography.titleMedium)

            Column(modifier = Modifier.selectableGroup()) {
                ModoTema.entries.forEach { modo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.modoTema == modo,
                                onClick = { onEvent(AjustesUiEvent.TemaElegido(modo)) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = state.modoTema == modo, onClick = null)
                        Text(
                            text = modo.etiqueta(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Notificaciones", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNotificacionesClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recordatorios del clima", style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }
    }
}

private fun ModoTema.etiqueta(): String = when (this) {
    ModoTema.SISTEMA -> "Automático (según el celular)"
    ModoTema.CLARO -> "Claro"
    ModoTema.OSCURO -> "Oscuro"
}

@Preview(showBackground = true)
@Composable
private fun PreviewAjustesClaro() {
    CambiateAppTheme(darkTheme = false) {
        AjustesScreen(AjustesUiState(modoTema = ModoTema.CLARO), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAjustesOscuro() {
    CambiateAppTheme(darkTheme = true) {
        AjustesScreen(AjustesUiState(modoTema = ModoTema.OSCURO), {}, {}, {})
    }
}