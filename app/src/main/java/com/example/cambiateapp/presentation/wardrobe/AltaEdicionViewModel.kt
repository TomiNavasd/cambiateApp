package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.GetPrendaUseCase
import com.example.cambiateapp.domain.usecase.SavePrendaUseCase
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
    private val savePrenda: SavePrendaUseCase
) : ViewModel() {

    // null = prenda nueva; con valor = editamos esa prenda.
    private val prendaId: String? = savedStateHandle.get<String>(Ruta.AltaEdicion.ARG_ID)

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
            is AltaEdicionUiEvent.CategoriaSeleccionada ->
                _uiState.update { it.copy(categoria = event.categoria) }
            is AltaEdicionUiEvent.ColoresCambio ->
                _uiState.update { it.copy(colores = event.valor) }
            is AltaEdicionUiEvent.OcasionAlternada -> _uiState.update {
                val nuevas = if (event.ocasion in it.ocasiones) it.ocasiones - event.ocasion
                else it.ocasiones + event.ocasion
                it.copy(ocasiones = nuevas)
            }
            is AltaEdicionUiEvent.FotoUrlCambio ->
                _uiState.update { it.copy(fotoUrl = event.valor, errorFoto = null) }
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

    private fun guardar() {
        val actual = _uiState.value
        if (actual.guardando) return
        _uiState.update {
            it.copy(guardando = true, errorNombre = null, errorFoto = null, errorGeneral = null)
        }

        val prenda = Prenda(
            id = prendaId.orEmpty(),
            nombre = actual.nombre,
            descripcion = actual.descripcion.trim(),
            categoria = actual.categoria,
            colores = parsearColores(actual.colores),
            ocasiones = Ocasion.entries.filter { it in actual.ocasiones },
            fotoUrl = actual.fotoUrl.trim()
        )

        viewModelScope.launch {
            savePrenda(prenda).fold(
                onSuccess = {
                    _uiState.update { it.copy(guardando = false, guardadoExitoso = true) }
                },
                onFailure = { error ->
                    _uiState.update { aplicarError(it, error) }
                }
            )
        }
    }

    private fun parsearColores(texto: String): List<String> =
        texto.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()

    private fun aplicarError(estado: AltaEdicionUiState, error: Throwable): AltaEdicionUiState {
        val base = estado.copy(guardando = false)
        return when {
            error is DomainException.Validacion -> when (error.campo) {
                CampoInvalido.NOMBRE -> base.copy(errorNombre = "Ingresá un nombre")
                CampoInvalido.FOTO -> base.copy(errorFoto = "Agregá la foto de la prenda")
                else -> base.copy(errorGeneral = "Revisá los datos ingresados")
            }
            else -> base.copy(errorGeneral = mensajeDe(error, guardando = true))
        }
    }

    private fun mensajeDe(error: Throwable, guardando: Boolean = false): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para modificar esta prenda"
        DomainException.SesionNoIniciada -> "Tu sesión venció. Iniciá sesión de nuevo"
        else -> if (guardando) "No pudimos guardar la prenda" else "No pudimos cargar la prenda"
    }
}