package com.example.cambiateapp.presentation.notifications

import androidx.lifecycle.ViewModel
import com.example.cambiateapp.domain.usecase.EnviarNotificacionDePruebaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val enviarNotificacionDePrueba: EnviarNotificacionDePruebaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    fun onEvent(evento: NotificacionesUiEvent) {
        when (evento) {
            NotificacionesUiEvent.ProbarAhora -> {
                val mostrada = enviarNotificacionDePrueba()
                _uiState.update {
                    it.copy(
                        aviso = if (mostrada) {
                            "Listo, mirá la barra de notificaciones"
                        } else {
                            "No hay permiso para mostrar notificaciones"
                        }
                    )
                }
            }
            NotificacionesUiEvent.PermisoRechazado -> _uiState.update {
                it.copy(aviso = "Sin permiso no podemos avisarte. Activalo en los ajustes del sistema.")
            }
            NotificacionesUiEvent.AvisoMostrado -> _uiState.update { it.copy(aviso = null) }
        }
    }
}