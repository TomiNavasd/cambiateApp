package com.example.cambiateapp.presentation.wardrobe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.presentation.components.AppTextField
import com.example.cambiateapp.presentation.components.CategoryChip
import com.example.cambiateapp.presentation.components.EmptyState
import com.example.cambiateapp.presentation.components.ErrorState
import com.example.cambiateapp.presentation.components.LoadingState
import com.example.cambiateapp.presentation.components.PrimaryButton
import com.example.cambiateapp.ui.theme.CambiateAppTheme

/** Capa con estado: conecta el ViewModel, arma los selectores de foto y avisa con callbacks. */
@Composable
fun AltaEdicionRoute(
    onGuardado: () -> Unit,
    onVolver: () -> Unit,
    viewModel: AltaEdicionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Uri donde la cámara va a dejar la foto. Se guarda para sobrevivir a una rotación.
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onEvent(AltaEdicionUiEvent.FotoSeleccionada(uri.toString()))
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { tomada ->
        val uri = uriCamara
        if (tomada && uri != null) viewModel.onEvent(AltaEdicionUiEvent.FotoSeleccionada(uri.toString()))
    }

    // Cuando se guardó bien, avisamos una sola vez para que la navegación vuelva atrás.
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) onGuardado()
    }

    AltaEdicionScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onGaleria = {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCamara = {
            val uri = crearUriParaCamara(context)
            uriCamara = uri
            camara.launch(uri)
        },
        onVolver = onVolver
    )
}

/** Pantalla "tonta": dibuja el estado y manda eventos hacia arriba. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaEdicionScreen(
    state: AltaEdicionUiState,
    onEvent: (AltaEdicionUiEvent) -> Unit,
    onGaleria: () -> Unit,
    onCamara: () -> Unit,
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
                else -> Formulario(
                    state = state,
                    onEvent = onEvent,
                    onGaleria = onGaleria,
                    onCamara = onCamara
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Formulario(
    state: AltaEdicionUiState,
    onEvent: (AltaEdicionUiEvent) -> Unit,
    onGaleria: () -> Unit,
    onCamara: () -> Unit
) {
    val habilitado = !state.guardando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SeccionFoto(
            foto = state.fotoMostrada,
            error = state.errorFoto,
            // No se puede cambiar la foto mientras se guarda ni mientras la IA analiza la anterior.
            habilitado = !state.guardando && !state.analizando,
            analizando = state.analizando,
            aviso = state.avisoEscaneo,
            onGaleria = onGaleria,
            onCamara = onCamara
        )

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
            cargando = state.guardando || state.analizando
        )
    }
}

@Composable
private fun SeccionFoto(
    foto: String,
    error: String?,
    habilitado: Boolean,
    analizando: Boolean,
    aviso: String?,
    onGaleria: () -> Unit,
    onCamara: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Foto", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (foto.isBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Todavía no elegiste una foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto de la prenda",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onGaleria,
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Galería")
            }
            OutlinedButton(
                onClick = onCamara,
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cámara")
            }
        }

        if (analizando) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Analizando prenda…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (aviso != null) {
            Text(
                text = aviso,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ---- Vistas previas ----

@Preview(showBackground = true)
@Composable
private fun PreviewNueva() {
    CambiateAppTheme { AltaEdicionScreen(AltaEdicionUiState(), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAnalizando() {
    CambiateAppTheme {
        AltaEdicionScreen(AltaEdicionUiState(analizando = true), {}, {}, {}, {})
    }
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
                errorFoto = "Elegí una foto para la prenda"
            ),
            {}, {}, {}, {}
        )
    }
}