package com.example.cambiateapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.usecase.ObtenerUbicacionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val obtenerUbicacion: ObtenerUbicacionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapaUiState>(MapaUiState.SinPermiso())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    fun onEvent(evento: MapaUiEvent) {
        when (evento) {
            MapaUiEvent.PermisoConcedido, MapaUiEvent.Reintentar -> cargarUbicacion()
            MapaUiEvent.PermisoRechazado -> _uiState.value = MapaUiState.SinPermiso(rechazado = true)
        }
    }

    private fun cargarUbicacion() {
        _uiState.value = MapaUiState.Cargando
        viewModelScope.launch {
            _uiState.value = try {
                val ubicacion = obtenerUbicacion()
                if (ubicacion != null) {
                    MapaUiState.Contenido(ubicacion)
                } else {
                    MapaUiState.Error("No pudimos obtener tu ubicación. Revisá que el GPS esté activado.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MapaUiState.Error("No pudimos obtener tu ubicación.")
            }
        }
    }
}