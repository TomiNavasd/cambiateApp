package com.example.cambiateapp.presentation.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.R
import com.example.cambiateapp.ui.theme.CambiateAppTheme

@Composable
fun NotificacionesRoute(
    onVolver: () -> Unit,
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pedirUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Alcanza con que acepte la ubicación aproximada o la precisa.
        viewModel.onEvent(
            if (resultado.values.any { it }) {
                NotificacionesUiEvent.UbicacionConcedida
            } else {
                NotificacionesUiEvent.UbicacionRechazada
            }
        )
    }

    fun asegurarUbicacion() {
        if (tienePermisoDeUbicacion(context)) {
            viewModel.onEvent(NotificacionesUiEvent.UbicacionConcedida)
        } else {
            pedirUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun aplicar(evento: NotificacionesUiEvent) {
        viewModel.onEvent(evento)
        if (evento is NotificacionesUiEvent.ActivarCambiado && evento.activo) asegurarUbicacion()
    }

    // Qué había que hacer cuando se pidió el permiso, para hacerlo si el usuario acepta.
    var accionPendiente by remember { mutableStateOf<NotificacionesUiEvent?>(null) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        val accion = accionPendiente
        accionPendiente = null
        if (concedido && accion != null) {
            aplicar(accion)
        } else {
            viewModel.onEvent(NotificacionesUiEvent.PermisoRechazado)
        }
    }

    fun ejecutarConPermiso(evento: NotificacionesUiEvent) {
        val faltaPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (faltaPermiso) {
            accionPendiente = evento
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            aplicar(evento)
        }
    }

    NotificacionesScreen(
        state = state,
        onEvent = { evento ->
            when {
                evento == NotificacionesUiEvent.ProbarAhora -> ejecutarConPermiso(evento)
                evento is NotificacionesUiEvent.ActivarCambiado && evento.activo -> ejecutarConPermiso(evento)
                else -> viewModel.onEvent(evento)
            }
        },
        onVolver = onVolver
    )
}

private fun tienePermisoDeUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    state: NotificacionesUiState,
    onEvent: (NotificacionesUiEvent) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Estado puramente visual: si el selector de hora está abierto.
    var selectorAbierto by remember { mutableStateOf(false) }

    LaunchedEffect(state.aviso) {
        state.aviso?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(NotificacionesUiEvent.AvisoMostrado)
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
                        text = stringResource(R.string.notif_titulo),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.notif_aviso_clima),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.notif_descripcion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.notif_recibir_diario),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.activo,
                    onCheckedChange = { onEvent(NotificacionesUiEvent.ActivarCambiado(it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.notif_hora),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedButton(onClick = { selectorAbierto = true }) {
                    Text("%02d:%02d".format(state.hora, state.minuto))
                }
            }

            Button(onClick = { onEvent(NotificacionesUiEvent.ProbarAhora) }) {
                Text(stringResource(R.string.notif_probar))
            }
        }
    }

    if (selectorAbierto) {
        val horaState = rememberTimePickerState(
            initialHour = state.hora,
            initialMinute = state.minuto,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { selectorAbierto = false },
            title = { Text(stringResource(R.string.notif_hora)) },
            text = { TimePicker(state = horaState) },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(NotificacionesUiEvent.HoraElegida(horaState.hour, horaState.minute))
                    selectorAbierto = false
                }) { Text(stringResource(R.string.accion_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { selectorAbierto = false }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNotificaciones() {
    CambiateAppTheme {
        NotificacionesScreen(NotificacionesUiState(activo = true), {}, {})
    }
}