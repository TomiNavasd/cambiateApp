package com.example.cambiateapp.presentation.outfits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.presentation.components.CategoryChip
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.presentation.components.PrimaryButton
import com.example.cambiateapp.presentation.wardrobe.etiqueta
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
                title = { Text("Mis outfits", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
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
            title = { Text("¿Eliminar outfit?") },
            text = { Text("Se borra el outfit, pero tus prendas se mantienen en el armario.") },
            confirmButton = {
                TextButton(onClick = {
                    outfitAEliminar = null
                    onEvent(OutfitsUiEvent.Eliminar(id))
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { outfitAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContenidoOutfits(
    state: OutfitsUiState.Contenido,
    onEvent: (OutfitsUiEvent) -> Unit,
    onPedirEliminar: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("¿Para qué ocasión?", style = MaterialTheme.typography.titleMedium)
        }
        item {
            // FlowRow: los chips pasan a la fila de abajo si no entran, así se ven todos.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Ocasion.entries.forEach { ocasion ->
                    CategoryChip(
                        texto = ocasion.etiqueta(),
                        seleccionado = state.ocasion == ocasion,
                        onClick = { onEvent(OutfitsUiEvent.OcasionSeleccionada(ocasion)) }
                    )
                }
            }
        }
        item {
            PrimaryButton(
                texto = "Sugerirme outfits",
                onClick = { onEvent(OutfitsUiEvent.Sugerir) },
                cargando = state.sugiriendo
            )
        }

        if (state.sugerencias.isNotEmpty()) {
            item { Text("Sugerencias", style = MaterialTheme.typography.titleMedium) }
            itemsIndexed(state.sugerencias) { indice, item ->
                OutfitCard(item = item) {
                    TextButton(onClick = { onEvent(OutfitsUiEvent.DescartarSugerencia(indice)) }) {
                        Text("Descartar")
                    }
                    TextButton(onClick = { onEvent(OutfitsUiEvent.GuardarSugerencia(indice)) }) {
                        Text("Guardar")
                    }
                }
            }
        }

        item { Text("Guardados", style = MaterialTheme.typography.titleMedium) }
        if (state.guardados.isEmpty()) {
            item {
                Text(
                    text = "Todavía no guardaste outfits. Pedí una sugerencia y guardá la que más te guste.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.guardados, key = { it.outfit.id }) { item ->
                OutfitCard(item = item) {
                    IconButton(onClick = { onEvent(OutfitsUiEvent.AlternarFavorito(item.outfit)) }) {
                        Icon(
                            imageVector = if (item.outfit.favorito) Icons.Rounded.Favorite
                            else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (item.outfit.favorito) "Quitar de favoritos"
                            else "Marcar como favorito"
                        )
                    }
                    IconButton(onClick = { onPedirEliminar(item.outfit.id) }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Eliminar outfit",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutfitCard(
    item: OutfitConPrendas,
    acciones: @Composable RowScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.outfit.ocasion.etiqueta(), style = MaterialTheme.typography.titleMedium)
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(item.prendas, key = { it.id }) { prenda -> MiniaturaPrenda(prenda) }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = acciones
            )
        }
    }
}

@Composable
private fun MiniaturaPrenda(prenda: Prenda) {
    Column(modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
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
        Text(
            text = prenda.nombre,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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