package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.DeletePrendaUseCase
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
import com.example.cambiateapp.presentation.common.ProveedorDeTextos
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PrendaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observePrendas: ObservePrendasUseCase,
    private val deletePrenda: DeletePrendaUseCase,
    private val textos: ProveedorDeTextos
) : ViewModel() {

    private data class Eliminacion(
        val enCurso: Boolean = false,
        val error: String? = null,
        val completada: Boolean = false
    )

    private val prendaId: String = savedStateHandle.get<String>(Ruta.Detalle.ARG_ID).orEmpty()
    private val reintentos = MutableStateFlow(0)
    private val eliminacion = MutableStateFlow(Eliminacion())

    val uiState: StateFlow<DetalleUiState> = reintentos
        .flatMapLatest {
            combine(observePrendas(), eliminacion) { prendas, elim ->
                armarEstado(prendas, elim)
            }
                .onStart { emit(DetalleUiState.Cargando) }
                .catch { emit(DetalleUiState.Error(mensajeDe(it))) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetalleUiState.Cargando
        )

    fun onEvent(event: DetalleUiEvent) {
        when (event) {
            DetalleUiEvent.Reintentar -> reintentos.update { it + 1 }
            DetalleUiEvent.Eliminar -> eliminar()
        }
    }

    private fun eliminar() {
        if (eliminacion.value.enCurso) return
        eliminacion.update { Eliminacion(enCurso = true) }
        viewModelScope.launch {
            deletePrenda(prendaId).fold(
                onSuccess = { eliminacion.update { Eliminacion(completada = true) } },
                onFailure = { e -> eliminacion.update { Eliminacion(error = mensajeEliminarDe(e)) } }
            )
        }
    }

    private fun armarEstado(prendas: List<Prenda>, elim: Eliminacion): DetalleUiState {
        val prenda = prendas.firstOrNull { it.id == prendaId }
        if (elim.completada || (elim.enCurso && prenda == null)) return DetalleUiState.Eliminada
        if (prenda == null) return DetalleUiState.NoEncontrada
        return DetalleUiState.Contenido(
            prenda = prenda,
            eliminando = elim.enCurso,
            errorEliminar = elim.error
        )
    }

    private fun mensajeDe(error: Throwable): String = textos.obtener(
        when (error) {
            DomainException.SinConexion -> R.string.error_sin_conexion
            DomainException.SinPermisos -> R.string.detalle_error_sin_permiso
            else -> R.string.alta_error_cargar
        }
    )

    private fun mensajeEliminarDe(error: Throwable): String = textos.obtener(
        when (error) {
            DomainException.SinConexion -> R.string.detalle_error_eliminar_sin_conexion
            DomainException.SinPermisos -> R.string.detalle_error_eliminar_sin_permiso
            DomainException.SesionNoIniciada -> R.string.error_sesion_vencida
            else -> R.string.detalle_error_eliminar
        }
    )
}