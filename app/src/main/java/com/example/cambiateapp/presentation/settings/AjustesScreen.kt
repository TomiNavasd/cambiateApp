package com.example.cambiateapp.presentation.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.ui.theme.CambiateAppTheme

@Composable
fun AjustesRoute(
    onVolver: () -> Unit,
    onNotificacionesClick: () -> Unit,
    viewModel: AjustesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val idiomaActual = remember { GestorIdioma.actual(context) }

    AjustesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        idiomaActual = idiomaActual,
        idiomaDisponible = GestorIdioma.disponible,
        onIdiomaElegido = { GestorIdioma.aplicar(context, it) },
        onNotificacionesClick = onNotificacionesClick,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    state: AjustesUiState,
    onEvent: (AjustesUiEvent) -> Unit,
    idiomaActual: IdiomaApp,
    idiomaDisponible: Boolean,
    onIdiomaElegido: (IdiomaApp) -> Unit,
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
                title = {
                    Text(
                        text = stringResource(R.string.ajustes_titulo),
                        style = MaterialTheme.typography.headlineSmall
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ajustes_apariencia),
                style = MaterialTheme.typography.titleMedium
            )

            Column(modifier = Modifier.selectableGroup()) {
                ModoTema.entries.forEach { modo ->
                    OpcionRadio(
                        texto = stringResource(modo.etiquetaRes()),
                        seleccionada = state.modoTema == modo,
                        onClick = { onEvent(AjustesUiEvent.TemaElegido(modo)) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.ajustes_idioma),
                style = MaterialTheme.typography.titleMedium
            )

            if (idiomaDisponible) {
                Column(modifier = Modifier.selectableGroup()) {
                    IdiomaApp.entries.forEach { idioma ->
                        OpcionRadio(
                            texto = stringResource(idioma.etiquetaRes()),
                            seleccionada = idiomaActual == idioma,
                            onClick = { onIdiomaElegido(idioma) }
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.ajustes_idioma_no_disponible),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.ajustes_notificaciones),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNotificacionesClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.ajustes_recordatorios),
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun OpcionRadio(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionada,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null)
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@StringRes
private fun ModoTema.etiquetaRes(): Int = when (this) {
    ModoTema.SISTEMA -> R.string.tema_automatico
    ModoTema.CLARO -> R.string.tema_claro
    ModoTema.OSCURO -> R.string.tema_oscuro
}

@StringRes
private fun IdiomaApp.etiquetaRes(): Int = when (this) {
    IdiomaApp.AUTOMATICO -> R.string.idioma_automatico
    IdiomaApp.ESPANOL -> R.string.idioma_espanol
    IdiomaApp.INGLES -> R.string.idioma_ingles
    IdiomaApp.PORTUGUES -> R.string.idioma_portugues
}

@Preview(showBackground = true)
@Composable
private fun PreviewAjustesClaro() {
    CambiateAppTheme(darkTheme = false) {
        AjustesScreen(
            state = AjustesUiState(modoTema = ModoTema.CLARO),
            onEvent = {},
            idiomaActual = IdiomaApp.AUTOMATICO,
            idiomaDisponible = true,
            onIdiomaElegido = {},
            onNotificacionesClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAjustesOscuro() {
    CambiateAppTheme(darkTheme = true) {
        AjustesScreen(
            state = AjustesUiState(modoTema = ModoTema.OSCURO),
            onEvent = {},
            idiomaActual = IdiomaApp.INGLES,
            idiomaDisponible = true,
            onIdiomaElegido = {},
            onNotificacionesClick = {},
            onVolver = {}
        )
    }
}