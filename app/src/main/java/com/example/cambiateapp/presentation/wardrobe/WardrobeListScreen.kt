package com.example.cambiateapp.presentation.wardrobe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.presentation.components.CategoryChip
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.presentation.components.PrendaCard
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel con la pantalla. No navega: avisa con callbacks. */
@Composable
fun WardrobeListRoute(
    onPrendaClick: (String) -> Unit,
    onNuevaClick: () -> Unit,
    onOutfitsClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: WardrobeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WardrobeListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onPrendaClick = onPrendaClick,
        onNuevaClick = onNuevaClick,
        onOutfitsClick = onOutfitsClick,
        onCerrarSesion = onCerrarSesion
    )
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeListScreen(
    state: WardrobeUiState,
    onEvent: (WardrobeUiEvent) -> Unit,
    onPrendaClick: (String) -> Unit,
    onNuevaClick: () -> Unit,
    onOutfitsClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado puramente visual (menú y diálogo abiertos): vive en la pantalla.
    var menuAbierto by remember { mutableStateOf(false) }
    var dialogoAbierto by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi armario", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    TextButton(onClick = onOutfitsClick) { Text("Outfits") }
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                menuAbierto = false
                                dialogoAbierto = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state is WardrobeUiState.Contenido) {
                FloatingActionButton(
                    onClick = onNuevaClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Agregar prenda")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                WardrobeUiState.Cargando -> LoadingState()
                WardrobeUiState.Vacio -> EmptyState(
                    titulo = "Tu armario está vacío",
                    mensaje = "Comenzá agregando tus prendas para organizar tu ropa y crear outfits.",
                    textoBoton = "Agregar mi primera prenda",
                    onClick = onNuevaClick
                )
                is WardrobeUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onReintentar = { onEvent(WardrobeUiEvent.Reintentar) }
                )
                is WardrobeUiState.Contenido -> ContenidoArmario(
                    state = state,
                    onEvent = onEvent,
                    onPrendaClick = onPrendaClick
                )
            }
        }
    }

    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text("Vas a salir de tu cuenta. Tendrás que iniciar sesión nuevamente para acceder a tus prendas y outfits.")
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogoAbierto = false
                    onCerrarSesion()
                }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ContenidoArmario(
    state: WardrobeUiState.Contenido,
    onEvent: (WardrobeUiEvent) -> Unit,
    onPrendaClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    texto = "Todas",
                    seleccionado = state.filtro == null,
                    onClick = { onEvent(WardrobeUiEvent.FiltroSeleccionado(null)) }
                )
            }
            items(Categoria.entries) { categoria ->
                CategoryChip(
                    texto = categoria.etiqueta(),
                    seleccionado = state.filtro == categoria,
                    onClick = { onEvent(WardrobeUiEvent.FiltroSeleccionado(categoria)) }
                )
            }
        }

        if (state.prendas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No tenés prendas en esta categoría",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.prendas, key = { it.id }) { prenda ->
                    PrendaCard(
                        nombre = prenda.nombre,
                        categoria = prenda.categoria.etiqueta(),
                        fotoUrl = prenda.fotoUrl,
                        onClick = { onPrendaClick(prenda.id) }
                    )
                }
            }
        }
    }
}

// ---- Vistas previas: así se ven los cuatro estados sin correr la app ----

private val prendasDeEjemplo = listOf(
    Prenda("1", "Remera blanca básica", "Algodón", Categoria.SUPERIOR, listOf("blanco"), listOf(Ocasion.CASUAL), ""),
    Prenda("2", "Jean azul", "Corte recto", Categoria.INFERIOR, listOf("azul"), listOf(Ocasion.CASUAL), ""),
    Prenda("3", "Zapatillas blancas", "Para todos los días", Categoria.CALZADO, listOf("blanco"), listOf(Ocasion.CASUAL), ""),
    Prenda("4", "Campera de jean", "Con botones metálicos", Categoria.ABRIGO, listOf("azul"), listOf(Ocasion.CASUAL), "")
)

@Preview(showBackground = true)
@Composable
private fun PreviewContenido() {
    CambiateAppTheme {
        WardrobeListScreen(WardrobeUiState.Contenido(prendasDeEjemplo, null), {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCargando() {
    CambiateAppTheme { WardrobeListScreen(WardrobeUiState.Cargando, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewVacio() {
    CambiateAppTheme { WardrobeListScreen(WardrobeUiState.Vacio, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewError() {
    CambiateAppTheme {
        WardrobeListScreen(WardrobeUiState.Error("No pudimos cargar tu armario"), {}, {}, {}, {}, {})
    }
}