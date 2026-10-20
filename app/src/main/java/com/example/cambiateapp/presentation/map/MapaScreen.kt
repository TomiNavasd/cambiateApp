package com.example.cambiateapp.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import java.io.File
import kotlin.math.roundToInt
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * De dónde salen las imágenes del mapa. Si los servidores de OpenStreetMap bloquean los pedidos
 * (se ven carteles "Access blocked"), cambiar MAPNIK por TileSourceFactory.OpenTopo.
 */
private val FuenteDeMapa = TileSourceFactory.OpenTopo

/** Desde este porcentaje de lluvia se muestra el paraguas en vez del termómetro. */
private const val LLUVIA_PARA_PARAGUAS_PCT = 50

/** Capa con estado: conecta el ViewModel y se ocupa del permiso de ubicación. */
@Composable
fun MapaRoute(
    onVolver: () -> Unit,
    onSugerirClick: () -> Unit,
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
        onSugerirClick = onSugerirClick,
        onVolver = onVolver
    )
}

/** Pantalla "tonta": dibuja el estado y avisa con callbacks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    state: MapaUiState,
    onPedirPermiso: () -> Unit,
    onReintentar: () -> Unit,
    onSugerirClick: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mapa", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
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
                    // La licencia del mapa exige mostrar esta atribución (sale de la fuente elegida).
                    Text(
                        text = FuenteDeMapa.copyrightNotice,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(4.dp)
                    )
                    TarjetaClima(
                        clima = state.clima,
                        cargando = state.cargandoClima,
                        onSugerirClick = onSugerirClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

/** Tarjeta sobre el mapa: dónde estás, el clima de mañana y el botón para sugerir outfits. */
@Composable
private fun TarjetaClima(
    clima: ClimaUi?,
    cargando: Boolean,
    onSugerirClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Tu ubicación",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            when {
                clima != null -> ContenidoClima(clima)
                cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Buscando el clima…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> Text(
                    text = "No pudimos obtener el clima por ahora.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSugerirClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sugerir", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ContenidoClima(clima: ClimaUi) {
    val pronostico = clima.pronostico
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (pronostico.probLluviaPct >= LLUVIA_PARA_PARAGUAS_PCT) Icons.Rounded.Umbrella
            else Icons.Rounded.Thermostat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Column {
            Text(
                text = "Mañana · ${pronostico.tempMinC.roundToInt()}° / ${pronostico.tempMaxC.roundToInt()}°",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "${pronostico.probLluviaPct}% de probabilidad de lluvia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Column {
        Text(
            text = clima.consejo.titulo,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = clima.consejo.mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Mapa de osmdroid (una vista clásica de Android) dentro de Compose. */
@Composable
private fun VistaDeMapa(ubicacion: Coordenadas, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        configurarOsmdroid(context)
        MapView(context).apply {
            setTileSource(FuenteDeMapa)
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

/** osmdroid guarda las imágenes del mapa en la caché de la app (no pide permiso de almacenamiento). */
private fun configurarOsmdroid(context: Context) {
    val configuracion = Configuration.getInstance()
    configuracion.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    // Los servidores de mapas piden un User-Agent que identifique a la app.
    configuracion.userAgentValue = "CambiateApp/1.0 (trabajo practico UNAJ; app Android)"
    val base = File(context.cacheDir, "osmdroid")
    configuracion.osmdroidBasePath = base
    // Carpeta nueva: así no se reutilizan cuadros bloqueados o con error de pruebas anteriores.
    configuracion.osmdroidTileCache = File(base, "tiles_v3")
}

@Preview(showBackground = true)
@Composable
private fun PreviewSinPermiso() {
    CambiateAppTheme { MapaScreen(MapaUiState.SinPermiso(), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewError() {
    CambiateAppTheme {
        MapaScreen(MapaUiState.Error("No pudimos obtener tu ubicación."), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTarjetaClima() {
    CambiateAppTheme {
        TarjetaClima(
            clima = ClimaUi(
                pronostico = Pronostico(tempMinC = 14.0, tempMaxC = 22.0, probLluviaPct = 30),
                consejo = ConsejoClima(
                    titulo = "Mañana, clima agradable",
                    mensaje = "No necesitás abrigo especial. ¡Elegí el outfit que más te guste!",
                    prendasSugeridas = emptyList()
                )
            ),
            cargando = false,
            onSugerirClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}