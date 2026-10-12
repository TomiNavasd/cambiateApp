package com.example.cambiateapp.presentation.wardrobe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.presentation.components.AppTextField
import com.example.cambiateapp.presentation.components.CategoryChip
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.presentation.components.PrimaryButton
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel con la pantalla. No navega: avisa con callbacks. */
@Composable
fun AltaEdicionRoute(
    onGuardado: () -> Unit,
    onVolver: () -> Unit,
    viewModel: AltaEdicionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando se guardó bien, avisamos una sola vez para que la navegación vuelva atrás.
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) onGuardado()
    }

    AltaEdicionScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onVolver = onVolver
    )
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaEdicionScreen(
    state: AltaEdicionUiState,
    onEvent: (AltaEdicionUiEvent) -> Unit,
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
                        text = if (state.esEdicion) "Editar prenda" else "Nueva prenda",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
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
            when {
                state.cargando -> LoadingState()
                state.noEncontrada -> EmptyState(
                    titulo = "No encontramos esta prenda",
                    mensaje = "Puede que se haya eliminado.",
                    textoBoton = "Volver",
                    onClick = onVolver
                )
                state.errorCarga != null -> ErrorState(
                    mensaje = state.errorCarga,
                    onReintentar = { onEvent(AltaEdicionUiEvent.ReintentarCarga) }
                )
                else -> Formulario(state = state, onEvent = onEvent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Formulario(
    state: AltaEdicionUiState,
    onEvent: (AltaEdicionUiEvent) -> Unit
) {
    val habilitado = !state.guardando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTextField(
            value = state.nombre,
            onValueChange = { onEvent(AltaEdicionUiEvent.NombreCambio(it)) },
            label = "Nombre",
            leadingIcon = Icons.Rounded.Edit,
            error = state.errorNombre,
            enabled = habilitado
        )

        AppTextField(
            value = state.descripcion,
            onValueChange = { onEvent(AltaEdicionUiEvent.DescripcionCambio(it)) },
            label = "Descripción (opcional)",
            leadingIcon = Icons.Rounded.Description,
            enabled = habilitado
        )

        Text(text = "Categoría", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Categoria.entries.forEach { categoria ->
                CategoryChip(
                    texto = categoria.etiqueta(),
                    seleccionado = state.categoria == categoria,
                    onClick = { onEvent(AltaEdicionUiEvent.CategoriaSeleccionada(categoria)) }
                )
            }
        }

        Text(text = "Ocasiones", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Ocasion.entries.forEach { ocasion ->
                CategoryChip(
                    texto = ocasion.etiqueta(),
                    seleccionado = ocasion in state.ocasiones,
                    onClick = { onEvent(AltaEdicionUiEvent.OcasionAlternada(ocasion)) }
                )
            }
        }

        AppTextField(
            value = state.colores,
            onValueChange = { onEvent(AltaEdicionUiEvent.ColoresCambio(it)) },
            label = "Colores",
            leadingIcon = Icons.Rounded.Palette,
            ayuda = "Separados por coma. Ej: blanco, azul",
            enabled = habilitado
        )

        AppTextField(
            value = state.fotoUrl,
            onValueChange = { onEvent(AltaEdicionUiEvent.FotoUrlCambio(it)) },
            label = "URL de la foto",
            leadingIcon = Icons.Rounded.Link,
            ayuda = "Provisorio: más adelante se elige de la galería o la cámara",
            error = state.errorFoto,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
            enabled = habilitado
        )

        if (state.errorGeneral != null) {
            Text(
                text = state.errorGeneral,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        PrimaryButton(
            texto = "Guardar prenda",
            onClick = { onEvent(AltaEdicionUiEvent.Guardar) },
            cargando = state.guardando
        )
    }
}

// ---- Vistas previas ----

@Preview(showBackground = true)
@Composable
private fun PreviewNueva() {
    CambiateAppTheme { AltaEdicionScreen(AltaEdicionUiState(), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewEdicionConError() {
    CambiateAppTheme {
        AltaEdicionScreen(
            AltaEdicionUiState(
                esEdicion = true,
                nombre = "",
                categoria = Categoria.CALZADO,
                ocasiones = setOf(Ocasion.CASUAL, Ocasion.SALIDA),
                colores = "negro, blanco",
                errorNombre = "Ingresá un nombre",
                errorFoto = "Agregá la foto de la prenda"
            ),
            {}, {}
        )
    }
}