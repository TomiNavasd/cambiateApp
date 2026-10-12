package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.DeletePrendaUseCase
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
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
    private val deletePrenda: DeletePrendaUseCase
) : ViewModel() {

    // Cómo va el borrado: en curso, con error o completado.
    private data class Eliminacion(
        val enCurso: Boolean = false,
        val error: String? = null,
        val completada: Boolean = false
    )

    // El id llega como argumento de la ruta "detalle/{id}".
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
        // Si la prenda desapareció mientras la estábamos borrando, no es "no encontrada": se borró.
        if (elim.completada || (elim.enCurso && prenda == null)) return DetalleUiState.Eliminada
        if (prenda == null) return DetalleUiState.NoEncontrada
        return DetalleUiState.Contenido(
            prenda = prenda,
            eliminando = elim.enCurso,
            errorEliminar = elim.error
        )
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para ver esta prenda"
        else -> "No pudimos cargar la prenda"
    }

    private fun mensajeEliminarDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. No pudimos eliminar la prenda"
        DomainException.SinPermisos -> "No tenés permiso para eliminar esta prenda"
        DomainException.SesionNoIniciada -> "Tu sesión venció. Iniciá sesión de nuevo"
        else -> "No pudimos eliminar la prenda"
    }
}