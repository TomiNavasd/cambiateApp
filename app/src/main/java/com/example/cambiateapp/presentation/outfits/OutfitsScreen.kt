package com.example.cambiateapp.presentation.outfits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.presentation.wardrobe.detalleCortoLocalizado
import com.example.cambiateapp.presentation.wardrobe.etiquetaRes
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import java.time.Instant

/** Capa con estado: conecta el ViewModel con la pantalla. No navega: avisa con callbacks. */
@Composable
fun OutfitsRoute(
    onVolver: () -> Unit,
    viewModel: OutfitsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OutfitsScreen(state = state, onEvent = viewModel::onEvent, onVolver = onVolver)
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitsScreen(
    state: OutfitsUiState,
    onEvent: (OutfitsUiEvent) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Estado puramente visual: qué outfit se está por eliminar (para el diálogo).
    var outfitAEliminar by remember { mutableStateOf<String?>(null) }

    val aviso = (state as? OutfitsUiState.Contenido)?.aviso
    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(aviso)
            onEvent(OutfitsUiEvent.AvisoMostrado)
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
                        text = stringResource(R.string.outfits_titulo),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.accion_volver)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                OutfitsUiState.Cargando -> LoadingState()
                is OutfitsUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onReintentar = { onEvent(OutfitsUiEvent.Reintentar) }
                )
                is OutfitsUiState.Contenido -> ContenidoOutfits(
                    state = state,
                    onEvent = onEvent,
                    onPedirEliminar = { outfitAEliminar = it }
                )
            }
        }
    }

    outfitAEliminar?.let { id ->
        AlertDialog(
            onDismissRequest = { outfitAEliminar = null },
            title = { Text(stringResource(R.string.outfits_eliminar_titulo)) },
            text = { Text(stringResource(R.string.outfits_eliminar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    outfitAEliminar = null
                    onEvent(OutfitsUiEvent.Eliminar(id))
                }) {
                    Text(
                        text = stringResource(R.string.accion_eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { outfitAEliminar = null }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

/** Qué outfit tiene abierta la hoja de detalles: una sugerencia (por posición) o uno guardado (por id). */
private data class Seleccion(val esSugerencia: Boolean, val clave: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContenidoOutfits(
    state: OutfitsUiState.Contenido,
    onEvent: (OutfitsUiEvent) -> Unit,
    onPedirEliminar: (String) -> Unit
) {
    var seleccion by remember { mutableStateOf<Seleccion?>(null) }

    val elegido = seleccion?.let { s ->
        if (s.esSugerencia) state.sugerencias.getOrNull(s.clave.toInt())
        else state.guardados.firstOrNull { it.outfit.id == s.clave }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                // FlowRow: los chips pasan a la fila de abajo si no entran, así se ven todos.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Ocasion.entries.forEach { ocasion ->
                        OcasionChip(
                            ocasion = ocasion,
                            seleccionada = state.ocasion == ocasion,
                            onClick = { onEvent(OutfitsUiEvent.OcasionSeleccionada(ocasion)) }
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) { BannerSugeridos() }

            if (state.sugerencias.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.outfits_sugerencias),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                itemsIndexed(state.sugerencias) { indice, item ->
                    OutfitTarjeta(
                        item = item,
                        favorito = false,
                        descripcionCorazon = stringResource(R.string.outfits_guardar),
                        onCorazon = { onEvent(OutfitsUiEvent.GuardarSugerencia(indice)) },
                        onVerDetalles = { seleccion = Seleccion(true, indice.toString()) }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.outfits_guardados),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (state.guardados.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.outfits_sin_guardados),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.guardados, key = { it.outfit.id }) { item ->
                    OutfitTarjeta(
                        item = item,
                        favorito = item.outfit.favorito,
                        descripcionCorazon = stringResource(
                            if (item.outfit.favorito) R.string.outfits_quitar_favorito
                            else R.string.outfits_marcar_favorito
                        ),
                        onCorazon = { onEvent(OutfitsUiEvent.AlternarFavorito(item.outfit)) },
                        onVerDetalles = { seleccion = Seleccion(false, item.outfit.id) }
                    )
                }
            }
        }

        BotonGenerar(
            texto = stringResource(
                if (state.sugerencias.isEmpty()) R.string.outfits_sugerir else R.string.outfits_generar_mas
            ),
            cargando = state.sugiriendo,
            onClick = { onEvent(OutfitsUiEvent.Sugerir) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )
    }

    if (seleccion != null && elegido == null) {
        // El outfit ya no existe (se guardó, se descartó o se eliminó): se cierra la hoja.
        LaunchedEffect(seleccion) { seleccion = null }
    }

    if (elegido != null) {
        val esSugerencia = seleccion?.esSugerencia == true
        DetalleOutfitSheet(
            item = elegido,
            esSugerencia = esSugerencia,
            onCerrar = { seleccion = null },
            onGuardar = {
                onEvent(OutfitsUiEvent.GuardarSugerencia(seleccion?.clave?.toInt() ?: 0))
                seleccion = null
            },
            onDescartar = {
                onEvent(OutfitsUiEvent.DescartarSugerencia(seleccion?.clave?.toInt() ?: 0))
                seleccion = null
            },
            onEliminar = {
                onPedirEliminar(elegido.outfit.id)
                seleccion = null
            }
        )
    }
}

@Composable
private fun OcasionChip(
    ocasion: Ocasion,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        selected = seleccionada,
        onClick = onClick,
        shape = CircleShape,
        color = if (seleccionada) colores.primaryContainer else colores.surface,
        contentColor = if (seleccionada) colores.onPrimaryContainer else colores.onSurface,
        border = if (seleccionada) null else BorderStroke(1.dp, colores.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = ocasion.icono(),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(ocasion.etiquetaRes()),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BannerSugeridos() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = stringResource(R.string.outfits_banner_titulo),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.outfits_banner_texto),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun BotonGenerar(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = modifier.height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OutfitTarjeta(
    item: OutfitConPrendas,
    favorito: Boolean,
    descripcionCorazon: String,
    onCorazon: () -> Unit,
    onVerDetalles: () -> Unit
) {
    val clavesTemporada = item.temporadasComunes()
    val temporada = textoDeTemporadas(clavesTemporada)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.prendas.take(4).forEach { prenda ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (prenda.fotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = prenda.fotoUrl,
                                    contentDescription = prenda.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(32.dp)
                ) {
                    IconButton(onClick = onCorazon) {
                        Icon(
                            imageVector = if (favorito) Icons.Rounded.Favorite
                            else Icons.Rounded.FavoriteBorder,
                            contentDescription = descripcionCorazon,
                            tint = if (favorito) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)) {
                Text(
                    text = item.nombreVisibleLocalizado(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DatoConIcono(
                        icono = Icons.Rounded.CalendarToday,
                        texto = stringResource(item.outfit.ocasion.etiquetaRes())
                    )
                    if (temporada != null) {
                        DatoConIcono(icono = iconoPorTemporada(clavesTemporada), texto = temporada)
                    }
                }
            }

            OutlinedButton(
                onClick = onVerDetalles,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.outfits_ver_detalles),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DatoConIcono(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleOutfitSheet(
    item: OutfitConPrendas,
    esSugerencia: Boolean,
    onCerrar: () -> Unit,
    onGuardar: () -> Unit,
    onDescartar: () -> Unit,
    onEliminar: () -> Unit
) {
    val clavesTemporada = item.temporadasComunes()
    val temporada = textoDeTemporadas(clavesTemporada)

    ModalBottomSheet(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = item.nombreVisibleLocalizado(), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DatoConIcono(
                    icono = Icons.Rounded.CalendarToday,
                    texto = stringResource(item.outfit.ocasion.etiquetaRes())
                )
                if (temporada != null) {
                    DatoConIcono(icono = iconoPorTemporada(clavesTemporada), texto = temporada)
                }
            }

            item.prendas.forEach { prenda ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (prenda.fotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = prenda.fotoUrl,
                                contentDescription = prenda.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column {
                        Text(
                            text = prenda.nombre,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(
                                stringResource(prenda.categoria.etiquetaRes()),
                                prenda.detalleCortoLocalizado()
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (esSugerencia) {
                Button(
                    onClick = onGuardar,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.outfits_guardar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = onDescartar,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.outfits_descartar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                TextButton(
                    onClick = onEliminar,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.outfits_eliminar),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ---- Vistas previas ----

private val remera = Prenda("1", "Remera blanca", "", Categoria.SUPERIOR, listOf("blanco"), listOf(Ocasion.CASUAL), "")
private val jean = Prenda("2", "Jean azul", "", Categoria.INFERIOR, listOf("azul"), listOf(Ocasion.CASUAL), "")
private val zapas = Prenda("3", "Zapatillas", "", Categoria.CALZADO, listOf("blanco"), listOf(Ocasion.CASUAL), "")

private fun outfitEjemplo(id: String, favorito: Boolean) = OutfitConPrendas(
    Outfit(id, Ocasion.CASUAL, listOf("1", "2", "3"), Instant.EPOCH, favorito),
    listOf(remera, jean, zapas)
)

@Preview(showBackground = true)
@Composable
private fun PreviewContenido() {
    CambiateAppTheme {
        OutfitsScreen(
            state = OutfitsUiState.Contenido(
                ocasion = Ocasion.CASUAL,
                sugerencias = listOf(outfitEjemplo("", false)),
                guardados = listOf(outfitEjemplo("o1", true)),
                sugiriendo = false,
                aviso = null
            ),
            onEvent = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCargando() {
    CambiateAppTheme { OutfitsScreen(OutfitsUiState.Cargando, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewError() {
    CambiateAppTheme { OutfitsScreen(OutfitsUiState.Error("No pudimos cargar tus outfits"), {}, {}) }
}