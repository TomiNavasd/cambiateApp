package com.example.cambiateapp.presentation.wardrobe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.Categoria
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
    onEditarClick: (String) -> Unit,
    onNuevaClick: () -> Unit,
    onAjustesClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: WardrobeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WardrobeListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onPrendaClick = onPrendaClick,
        onEditarClick = onEditarClick,
        onNuevaClick = onNuevaClick,
        onAjustesClick = onAjustesClick,
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
    onEditarClick: (String) -> Unit,
    onNuevaClick: () -> Unit,
    onAjustesClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado puramente visual: menú, diálogo, texto del buscador y foco.
    var menuAbierto by remember { mutableStateOf(false) }
    var dialogoAbierto by remember { mutableStateOf(false) }
    var texto by rememberSaveable { mutableStateOf("") }
    val foco = remember { FocusRequester() }
    val administradorDeFoco = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi armario", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (state is WardrobeUiState.Contenido) {
                        IconButton(onClick = { foco.requestFocus() }) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "Buscar")
                        }
                    }
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(
                            text = { Text("Ajustes") },
                            onClick = {
                                menuAbierto = false
                                onAjustesClick()
                            }
                        )
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
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
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
                is WardrobeUiState.Contenido -> Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = {
                            texto = it
                            onEvent(WardrobeUiEvent.BusquedaCambiada(it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .focusRequester(foco),
                        placeholder = { Text("Buscar prendas") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (texto.isNotEmpty()) {
                                IconButton(onClick = {
                                    texto = ""
                                    onEvent(WardrobeUiEvent.BusquedaCambiada(""))
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Borrar búsqueda"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { administradorDeFoco.clearFocus() })
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "No encontramos prendas con ese filtro",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.prendas, key = { it.id }) { prenda ->
                                PrendaCard(
                                    nombre = prenda.nombre,
                                    categoria = prenda.categoria.etiqueta(),
                                    fotoUrl = prenda.fotoUrl,
                                    onClick = { onPrendaClick(prenda.id) },
                                    detalle = prenda.detalleCorto(),
                                    onEditar = { onEditarClick(prenda.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que querés cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    dialogoAbierto = false
                    onCerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeListVacioPreview() {
    CambiateAppTheme {
        WardrobeListScreen(
            state = WardrobeUiState.Vacio,
            onEvent = {},
            onPrendaClick = {},
            onEditarClick = {},
            onNuevaClick = {},
            onAjustesClick = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeListErrorPreview() {
    CambiateAppTheme {
        WardrobeListScreen(
            state = WardrobeUiState.Error("Sin conexión. Revisá tu internet e intentá de nuevo"),
            onEvent = {},
            onPrendaClick = {},
            onEditarClick = {},
            onNuevaClick = {},
            onAjustesClick = {},
            onCerrarSesion = {}
        )
    }
}