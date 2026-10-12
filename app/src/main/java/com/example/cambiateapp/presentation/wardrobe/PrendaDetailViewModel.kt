package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PrendaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observePrendas: ObservePrendasUseCase
) : ViewModel() {

    // El id llega como argumento de la ruta "detalle/{id}".
    private val prendaId: String = savedStateHandle.get<String>(Ruta.Detalle.ARG_ID).orEmpty()
    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<DetalleUiState> = reintentos
        .flatMapLatest {
            observePrendas()
                .map { prendas -> armarEstado(prendas) }
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
        }
    }

    private fun armarEstado(prendas: List<Prenda>): DetalleUiState {
        val prenda = prendas.firstOrNull { it.id == prendaId }
        return if (prenda == null) DetalleUiState.NoEncontrada else DetalleUiState.Contenido(prenda)
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para ver esta prenda"
        else -> "No pudimos cargar la prenda"
    }
}