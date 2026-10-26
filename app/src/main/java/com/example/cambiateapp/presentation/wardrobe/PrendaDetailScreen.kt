package com.example.cambiateapp.presentation.wardrobe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel con la pantalla. No navega: avisa con callbacks. */
@Composable
fun PrendaDetailRoute(
    onEditar: (String) -> Unit,
    onEliminada: () -> Unit,
    onVolver: () -> Unit,
    viewModel: PrendaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando la prenda se eliminó bien, avisamos para que la navegación vuelva a la Lista.
    LaunchedEffect(state) {
        if (state is DetalleUiState.Eliminada) onEliminada()
    }

    PrendaDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onEditar = onEditar,
        onVolver = onVolver
    )
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrendaDetailScreen(
    state: DetalleUiState,
    onEvent: (DetalleUiEvent) -> Unit,
    onEditar: (String) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detalle_titulo),
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                DetalleUiState.Cargando, DetalleUiState.Eliminada -> LoadingState()
                DetalleUiState.NoEncontrada -> EmptyState(
                    titulo = stringResource(R.string.detalle_no_encontrada_titulo),
                    mensaje = stringResource(R.string.detalle_no_encontrada_mensaje),
                    textoBoton = stringResource(R.string.accion_volver),
                    onClick = onVolver
                )
                is DetalleUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onReintentar = { onEvent(DetalleUiEvent.Reintentar) }
                )
                is DetalleUiState.Contenido -> ContenidoDetalle(
                    state = state,
                    onEditar = onEditar,
                    onEliminar = { onEvent(DetalleUiEvent.Eliminar) }
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    state: DetalleUiState.Contenido,
    onEditar: (String) -> Unit,
    onEliminar: () -> Unit
) {
    val prenda = state.prenda
    // Estado puramente visual (diálogo abierto): vive en la pantalla.
    var dialogoAbierto by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = prenda.fotoUrl,
                contentDescription = prenda.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(text = prenda.nombre, style = MaterialTheme.typography.headlineMedium)

            if (prenda.descripcion.isNotBlank()) {
                Text(
                    text = prenda.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SeccionEtiquetas(
                titulo = stringResource(R.string.detalle_categoria),
                etiquetas = listOf(stringResource(prenda.categoria.etiquetaRes()))
            )

            if (prenda.colores.isNotEmpty()) {
                SeccionEtiquetas(
                    titulo = stringResource(R.string.detalle_colores),
                    etiquetas = prenda.colores.map { color -> color.replaceFirstChar { it.uppercase() } }
                )
            }

            if (prenda.ocasiones.isNotEmpty()) {
                SeccionEtiquetas(
                    titulo = stringResource(R.string.detalle_ocasiones),
                    etiquetas = prenda.ocasiones.map { stringResource(it.etiquetaRes()) }
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.errorEliminar != null) {
                Text(
                    text = state.errorEliminar,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { onEditar(prenda.id) },
                enabled = !state.eliminando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.detalle_editar))
            }

            OutlinedButton(
                onClick = { dialogoAbierto = true },
                enabled = !state.eliminando,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (state.eliminando) R.string.detalle_eliminando else R.string.detalle_eliminar
                    )
                )
            }
        }
    }

    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false },
            title = { Text(stringResource(R.string.detalle_dialogo_titulo)) },
            text = { Text(stringResource(R.string.detalle_dialogo_texto, prenda.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    dialogoAbierto = false
                    onEliminar()
                }) {
                    Text(
                        text = stringResource(R.string.accion_eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) {
                    Text(stringResource(R.string.accion_cancelar))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeccionEtiquetas(titulo: String, etiquetas: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            etiquetas.forEach { Etiqueta(it) }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 6.dp))
        )
    }
}

// ---- Vistas previas ----

private val prendaDeEjemplo = Prenda(
    id = "1",
    nombre = "Remera blanca básica",
    descripcion = "Algodón, corte regular",
    categoria = Categoria.SUPERIOR,
    colores = listOf("blanco"),
    ocasiones = listOf(Ocasion.CASUAL, Ocasion.SALIDA),
    fotoUrl = ""
)

@Preview(showBackground = true)
@Composable
private fun PreviewDetalleContenido() {
    CambiateAppTheme {
        PrendaDetailScreen(DetalleUiState.Contenido(prendaDeEjemplo), {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetalleErrorAlEliminar() {
    CambiateAppTheme {
        PrendaDetailScreen(
            DetalleUiState.Contenido(prendaDeEjemplo, errorEliminar = "No pudimos eliminar la prenda"),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetalleCargando() {
    CambiateAppTheme { PrendaDetailScreen(DetalleUiState.Cargando, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetalleNoEncontrada() {
    CambiateAppTheme { PrendaDetailScreen(DetalleUiState.NoEncontrada, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDetalleError() {
    CambiateAppTheme {
        PrendaDetailScreen(DetalleUiState.Error("No pudimos cargar la prenda"), {}, {}, {})
    }
}