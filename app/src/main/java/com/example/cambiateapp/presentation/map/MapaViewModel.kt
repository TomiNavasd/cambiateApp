package com.example.cambiateapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.usecase.ArmarConsejoClimaUseCase
import com.example.cambiateapp.domain.usecase.ObtenerPronosticoDeMananaUseCase
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
    private val obtenerPronostico: ObtenerPronosticoDeMananaUseCase,
    private val armarConsejo: ArmarConsejoClimaUseCase,
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
            val ubicacion = try {
                obtenerUbicacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = MapaUiState.Error("No pudimos obtener tu ubicación.")
                return@launch
            }

            if (ubicacion == null) {
                _uiState.value = MapaUiState.Error(
                    "No pudimos obtener tu ubicación. Revisá que el GPS esté activado."
                )
                return@launch
            }

            // Primero se muestra el mapa; el clima llega después y no bloquea nada.
            _uiState.value = MapaUiState.Contenido(ubicacion = ubicacion, cargandoClima = true)

            val clima = try {
                val pronostico = obtenerPronostico(ubicacion)
                // Sin prendas: acá solo hace falta el titular y el mensaje general del clima.
                ClimaUi(pronostico, armarConsejo(pronostico, emptyList()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            _uiState.value = MapaUiState.Contenido(ubicacion = ubicacion, clima = clima)
        }
    }
}