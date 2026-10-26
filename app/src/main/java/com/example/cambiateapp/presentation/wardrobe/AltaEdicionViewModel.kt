package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.AnalisisPrenda
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.AnalizarPrendaUseCase
import com.example.cambiateapp.domain.usecase.EtiquetarPrendaUseCase
import com.example.cambiateapp.domain.usecase.GetPrendaUseCase
import com.example.cambiateapp.domain.usecase.SavePrendaUseCase
import com.example.cambiateapp.domain.usecase.UploadImageUseCase
import com.example.cambiateapp.presentation.common.ProveedorDeTextos
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AltaEdicionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPrenda: GetPrendaUseCase,
    private val savePrenda: SavePrendaUseCase,
    private val uploadImage: UploadImageUseCase,
    private val analizarPrenda: AnalizarPrendaUseCase,
    private val etiquetarPrenda: EtiquetarPrendaUseCase,
    private val textos: ProveedorDeTextos
) : ViewModel() {

    private val prendaId: String? = savedStateHandle.get<String>(Ruta.AltaEdicion.ARG_ID)

    private var etiquetas: List<String> = emptyList()

    private var categoriaTocada = false

    private val _uiState = MutableStateFlow(
        AltaEdicionUiState(esEdicion = prendaId != null, cargando = prendaId != null)
    )
    val uiState: StateFlow<AltaEdicionUiState> = _uiState.asStateFlow()

    init {
        if (prendaId != null) cargarPrenda(prendaId)
    }

    fun onEvent(event: AltaEdicionUiEvent) {
        when (event) {
            is AltaEdicionUiEvent.NombreCambio ->
                _uiState.update { it.copy(nombre = event.valor, errorNombre = null) }
            is AltaEdicionUiEvent.DescripcionCambio ->
                _uiState.update { it.copy(descripcion = event.valor) }
            is AltaEdicionUiEvent.CategoriaSeleccionada -> {
                categoriaTocada = true
                _uiState.update { it.copy(categoria = event.categoria) }
            }
            is AltaEdicionUiEvent.ColoresCambio ->
                _uiState.update { it.copy(colores = event.valor) }
            is AltaEdicionUiEvent.OcasionAlternada -> _uiState.update {
                val nuevas = if (event.ocasion in it.ocasiones) it.ocasiones - event.ocasion
                else it.ocasiones + event.ocasion
                it.copy(ocasiones = nuevas)
            }
            is AltaEdicionUiEvent.FotoSeleccionada -> {
                _uiState.update { it.copy(fotoLocalUri = event.uri, errorFoto = null) }
                escanear(event.uri)
            }
            AltaEdicionUiEvent.Guardar -> guardar()
            AltaEdicionUiEvent.ReintentarCarga -> prendaId?.let { cargarPrenda(it) }
        }
    }

    private fun cargarPrenda(id: String) {
        _uiState.update { it.copy(cargando = true, errorCarga = null, noEncontrada = false) }
        viewModelScope.launch {
            try {
                val prenda = getPrenda(id)
                if (prenda == null) {
                    _uiState.update { it.copy(cargando = false, noEncontrada = true) }
                } else {
                    etiquetas = prenda.etiquetas
                    categoriaTocada = true
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombre = prenda.nombre,
                            descripcion = prenda.descripcion,
                            categoria = prenda.categoria,
                            colores = prenda.colores.joinToString(", "),
                            ocasiones = prenda.ocasiones.toSet(),
                            fotoUrl = prenda.fotoUrl
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, errorCarga = mensajeDe(e)) }
            }
        }
    }

    private fun escanear(uri: String) {
        etiquetas = emptyList()
        _uiState.update { it.copy(analizando = true, avisoEscaneo = null) }

        viewModelScope.launch {
            val subida = uploadImage(uri)
            val url = subida.getOrNull()
            if (url == null) {
                val aviso = textos.obtener(
                    if (subida.exceptionOrNull() === DomainException.SinConexion) R.string.escaneo_sin_internet
                    else R.string.escaneo_fallo
                )
                _uiState.update { it.copy(analizando = false, avisoEscaneo = aviso) }
                return@launch
            }
            _uiState.update { it.copy(fotoUrl = url, fotoLocalUri = null) }

            val analisis = analizarPrenda(url, textos.idioma())
            if (analisis == null) {
                _uiState.update {
                    it.copy(analizando = false, avisoEscaneo = textos.obtener(R.string.escaneo_fallo))
                }
                return@launch
            }
            etiquetas = analisis.etiquetas
            _uiState.update {
                precargar(it, analisis).copy(
                    analizando = false,
                    avisoEscaneo = textos.obtener(R.string.escaneo_ok)
                )
            }
        }
    }

    private fun precargar(estado: AltaEdicionUiState, analisis: AnalisisPrenda): AltaEdicionUiState {
        val nombre = if (estado.nombre.isBlank()) analisis.nombre else estado.nombre
        return estado.copy(
            nombre = nombre,
            errorNombre = if (nombre.isNotBlank()) null else estado.errorNombre,
            descripcion = if (estado.descripcion.isBlank()) analisis.descripcion else estado.descripcion,
            colores = if (estado.colores.isBlank()) analisis.colores.joinToString(", ") else estado.colores,
            ocasiones = if (estado.ocasiones.isEmpty()) analisis.ocasiones.toSet() else estado.ocasiones,
            categoria = if (!categoriaTocada && analisis.categoria != null) analisis.categoria
            else estado.categoria
        )
    }

    private fun guardar() {
        val actual = _uiState.value
        if (actual.guardando || actual.analizando) return

        if (actual.nombre.isBlank()) {
            _uiState.update { it.copy(errorNombre = textos.obtener(R.string.alta_error_nombre)) }
            return
        }

        _uiState.update {
            it.copy(guardando = true, errorNombre = null, errorFoto = null, errorGeneral = null)
        }

        viewModelScope.launch {
            var fotoUrl = actual.fotoUrl

            val fotoLocal = actual.fotoLocalUri
            if (fotoLocal != null) {
                fotoUrl = uploadImage(fotoLocal).getOrElse { error ->
                    _uiState.update { aplicarError(it, error, alSubirFoto = true) }
                    return@launch
                }
                _uiState.update { it.copy(fotoUrl = fotoUrl, fotoLocalUri = null) }
            }

            if (etiquetas.isEmpty()) {
                etiquetas = etiquetarPrenda(fotoUrl)
            }

            val prenda = Prenda(
                id = prendaId.orEmpty(),
                nombre = actual.nombre,
                descripcion = actual.descripcion.trim(),
                categoria = actual.categoria,
                colores = parsearColores(actual.colores),
                ocasiones = Ocasion.entries.filter { it in actual.ocasiones },
                fotoUrl = fotoUrl,
                etiquetas = etiquetas
            )
            savePrenda(prenda).fold(
                onSuccess = {
                    _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
                },
                onFailure = { error ->
                    _uiState.update { aplicarError(it, error, alSubirFoto = false) }
                }
            )
        }
    }

    private fun parsearColores(texto: String): List<String> =
        texto.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

    private fun aplicarError(
        estado: AltaEdicionUiState,
        error: Throwable,
        alSubirFoto: Boolean
    ): AltaEdicionUiState {
        val base = estado.copy(guardando = false)
        return when {
            error is DomainException.Validacion -> when (error.campo) {
                CampoInvalido.NOMBRE -> base.copy(errorNombre = textos.obtener(R.string.alta_error_nombre))
                CampoInvalido.FOTO -> base.copy(errorFoto = textos.obtener(R.string.alta_error_foto))
                else -> base.copy(errorGeneral = textos.obtener(R.string.error_revisar_datos))
            }
            alSubirFoto && error === DomainException.SinConexion ->
                base.copy(errorGeneral = textos.obtener(R.string.alta_error_foto_sin_internet))
            alSubirFoto ->
                base.copy(errorGeneral = textos.obtener(R.string.alta_error_subir_foto))
            else -> base.copy(errorGeneral = mensajeDe(error, guardando = true))
        }
    }

    private fun mensajeDe(error: Throwable, guardando: Boolean = false): String = textos.obtener(
        when (error) {
            DomainException.SinConexion -> R.string.error_sin_conexion
            DomainException.SinPermisos -> R.string.alta_error_sin_permiso
            DomainException.SesionNoIniciada -> R.string.error_sesion_vencida
            else -> if (guardando) R.string.alta_error_guardar else R.string.alta_error_cargar
        }
    )
}