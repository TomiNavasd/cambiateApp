package com.example.cambiateapp.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapaRoute(
    onVolver: () -> Unit,
    viewModel: MapaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pedirPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        // Alcanza con que acepte la ubicación aproximada o la precisa.
        viewModel.onEvent(
            if (resultado.values.any { it }) MapaUiEvent.PermisoConcedido else MapaUiEvent.PermisoRechazado
        )
    }

    // Si ya había dado el permiso antes, no hace falta pedirlo de nuevo.
    LaunchedEffect(Unit) {
        if (tienePermisoDeUbicacion(context)) viewModel.onEvent(MapaUiEvent.PermisoConcedido)
    }

    MapaScreen(
        state = state,
        onPedirPermiso = {
            pedirPermisos.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onReintentar = { viewModel.onEvent(MapaUiEvent.Reintentar) },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    state: MapaUiState,
    onPedirPermiso: () -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mapa", style = MaterialTheme.typography.headlineSmall) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is MapaUiState.SinPermiso -> EmptyState(
                    titulo = "Necesitamos tu ubicación",
                    mensaje = if (state.rechazado) {
                        "Sin el permiso no podemos mostrarte en el mapa. Podés activarlo desde los ajustes del sistema."
                    } else {
                        "Para mostrarte en el mapa necesitamos permiso de ubicación."
                    },
                    textoBoton = "Dar permiso",
                    onClick = onPedirPermiso
                )
                MapaUiState.Cargando -> LoadingState()
                is MapaUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onReintentar = onReintentar
                )
                is MapaUiState.Contenido -> {
                    VistaDeMapa(ubicacion = state.ubicacion, modifier = Modifier.fillMaxSize())
                    // La licencia de OpenStreetMap exige mostrar esta atribución.
                    Text(
                        text = "© OpenStreetMap contributors",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VistaDeMapa(ubicacion: Coordenadas, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        configurarOsmdroid(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    // El mapa tiene que enterarse de cuándo la pantalla se pausa o se reanuda.
    DisposableEffect(lifecycleOwner, mapView) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observador)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observador)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { vista ->
            val punto = GeoPoint(ubicacion.latitud, ubicacion.longitud)
            vista.overlays.clear()
            vista.overlays.add(
                Marker(vista).apply {
                    position = punto
                    title = "Estás acá"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
            )
            vista.controller.setCenter(punto)
            vista.invalidate()
        }
    )
}

private fun tienePermisoDeUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

private fun configurarOsmdroid(context: Context) {
    val configuracion = Configuration.getInstance()
    configuracion.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    // OpenStreetMap exige identificar a la app que pide las imágenes.
    configuracion.userAgentValue = context.packageName
    val base = File(context.cacheDir, "osmdroid")
    configuracion.osmdroidBasePath = base
    configuracion.osmdroidTileCache = File(base, "tiles")
}

@Preview(showBackground = true)
@Composable
private fun PreviewSinPermiso() {
    CambiateAppTheme { MapaScreen(MapaUiState.SinPermiso(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewError() {
    CambiateAppTheme {
        MapaScreen(MapaUiState.Error("No pudimos obtener tu ubicación."), {}, {}, {})
    }
}