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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
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
    onVolver: () -> Unit,
    viewModel: PrendaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = { Text("Detalle", style = MaterialTheme.typography.headlineSmall) },
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
                DetalleUiState.Cargando -> LoadingState()
                DetalleUiState.NoEncontrada -> EmptyState(
                    titulo = "No encontramos esta prenda",
                    mensaje = "Puede que se haya eliminado.",
                    textoBoton = "Volver",
                    onClick = onVolver
                )
                is DetalleUiState.Error -> ErrorState(
                    mensaje = state.mensaje,
                    onReintentar = { onEvent(DetalleUiEvent.Reintentar) }
                )
                is DetalleUiState.Contenido -> ContenidoDetalle(
                    prenda = state.prenda,
                    onEditar = onEditar
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    prenda: Prenda,
    onEditar: (String) -> Unit
) {
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

            SeccionEtiquetas(titulo = "Categoría", etiquetas = listOf(prenda.categoria.etiqueta()))

            if (prenda.colores.isNotEmpty()) {
                SeccionEtiquetas(
                    titulo = "Colores",
                    etiquetas = prenda.colores.map { color -> color.replaceFirstChar { it.uppercase() } }
                )
            }

            if (prenda.ocasiones.isNotEmpty()) {
                SeccionEtiquetas(
                    titulo = "Ocasiones",
                    etiquetas = prenda.ocasiones.map { it.etiqueta() }
                )
            }
        }

        Button(
            onClick = { onEditar(prenda.id) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Editar prenda")
        }
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