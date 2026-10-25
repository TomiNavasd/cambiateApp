package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val etiquetarPrenda: EtiquetarPrendaUseCase
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
                    categoriaTocada = true // la prenda ya existe: su categoría es la que eligió el usuario
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
                // Sin internet se avisa con claridad; cualquier otra falla usa el aviso general.
                val aviso = if (subida.exceptionOrNull() === DomainException.SinConexion) {
                    AVISO_ESCANEO_SIN_INTERNET
                } else {
                    AVISO_SIN_ESCANEO
                }
                _uiState.update { it.copy(analizando = false, avisoEscaneo = aviso) }
                return@launch
            }
            // Ya subida: guardamos la URL, así al guardar no se vuelve a subir.
            _uiState.update { it.copy(fotoUrl = url, fotoLocalUri = null) }

            val analisis = analizarPrenda(url)
            if (analisis == null) {
                _uiState.update { it.copy(analizando = false, avisoEscaneo = AVISO_SIN_ESCANEO) }
                return@launch
            }
            etiquetas = analisis.etiquetas
            _uiState.update {
                precargar(it, analisis).copy(analizando = false, avisoEscaneo = AVISO_ESCANEO_OK)
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
        // Si la IA todavía está analizando, esperamos: así no se guarda a medias ni se sube dos veces.
        if (actual.guardando || actual.analizando) return

        // Evita subir la foto si el nombre está vacío: no tiene sentido gastar la subida.
        if (actual.nombre.isBlank()) {
            _uiState.update { it.copy(errorNombre = "Ingresá un nombre") }
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
                CampoInvalido.NOMBRE -> base.copy(errorNombre = "Ingresá un nombre")
                CampoInvalido.FOTO -> base.copy(errorFoto = "Elegí una foto para la prenda")
                else -> base.copy(errorGeneral = "Revisá los datos ingresados")
            }
            alSubirFoto && error === DomainException.SinConexion ->
                base.copy(errorGeneral = ERROR_FOTO_SIN_INTERNET)
            alSubirFoto ->
                base.copy(errorGeneral = "No pudimos subir la foto. Intentá de nuevo")
            else -> base.copy(errorGeneral = mensajeDe(error, guardando = true))
        }
    }

    private fun mensajeDe(error: Throwable, guardando: Boolean = false): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para modificar esta prenda"
        DomainException.SesionNoIniciada -> "Tu sesión venció. Iniciá sesión de nuevo"
        else -> if (guardando) "No pudimos guardar la prenda" else "No pudimos cargar la prenda"
    }

    private companion object {
        const val AVISO_ESCANEO_OK =
            "Completamos los datos con IA. Revisalos y corregí lo que haga falta."
        const val AVISO_SIN_ESCANEO =
            "No pudimos analizar la foto. Completá los datos a mano."
        const val AVISO_ESCANEO_SIN_INTERNET =
            "Sin conexión: la foto nueva necesita internet para analizarse y subirse. " +
                    "Podés completar los datos a mano y guardar cuando vuelva la conexión."
        const val ERROR_FOTO_SIN_INTERNET =
            "La foto nueva necesita internet para subirse. Conectate y volvé a tocar Guardar: " +
                    "los datos que escribiste no se pierden."
    }
}