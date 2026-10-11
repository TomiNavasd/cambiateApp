package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Prenda
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val observePrendas: ObservePrendasUseCase
) : ViewModel() {

    private val filtro = MutableStateFlow<Categoria?>(null)
    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<WardrobeUiState> = reintentos
        .flatMapLatest {
            combine(observePrendas(), filtro) { prendas, categoria ->
                armarEstado(prendas, categoria)
            }
                .onStart { emit(WardrobeUiState.Cargando) }
                .catch { emit(WardrobeUiState.Error(mensajeDe(it))) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WardrobeUiState.Cargando
        )

    fun onEvent(event: WardrobeUiEvent) {
        when (event) {
            is WardrobeUiEvent.FiltroSeleccionado -> filtro.value = event.categoria
            WardrobeUiEvent.Reintentar -> reintentos.update { it + 1 }
        }
    }

    private fun armarEstado(prendas: List<Prenda>, categoria: Categoria?): WardrobeUiState {
        if (prendas.isEmpty()) return WardrobeUiState.Vacio
        val visibles = if (categoria == null) prendas else prendas.filter { it.categoria == categoria }
        return WardrobeUiState.Contenido(prendas = visibles, filtro = categoria)
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para ver estas prendas"
        else -> "No pudimos cargar tu armario"
    }
}