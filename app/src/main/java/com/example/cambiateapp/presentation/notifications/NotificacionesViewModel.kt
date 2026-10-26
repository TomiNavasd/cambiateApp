package com.example.cambiateapp.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.usecase.ConfigurarRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.EnviarNotificacionDePruebaUseCase
import com.example.cambiateapp.domain.usecase.ObservarPreferenciasRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.ObtenerUbicacionUseCase
import com.example.cambiateapp.presentation.common.ProveedorDeTextos
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    observarPreferencias: ObservarPreferenciasRecordatorioUseCase,
    private val configurarRecordatorio: ConfigurarRecordatorioUseCase,
    private val enviarNotificacionDePrueba: EnviarNotificacionDePruebaUseCase,
    private val obtenerUbicacion: ObtenerUbicacionUseCase,
    private val textos: ProveedorDeTextos,
) : ViewModel() {

    private val aviso = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NotificacionesUiState> =
        combine(observarPreferencias(), aviso) { preferencias, aviso ->
            NotificacionesUiState(
                activo = preferencias.activo,
                hora = preferencias.hora,
                minuto = preferencias.minuto,
                aviso = aviso,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificacionesUiState())

    fun onEvent(evento: NotificacionesUiEvent) {
        when (evento) {
            NotificacionesUiEvent.ProbarAhora -> {
                val mostrada = enviarNotificacionDePrueba()
                aviso.value = textos.obtener(
                    if (mostrada) R.string.notif_aviso_probar_ok
                    else R.string.notif_aviso_sin_permiso_notif
                )
            }
            NotificacionesUiEvent.PermisoRechazado ->
                aviso.value = textos.obtener(R.string.notif_aviso_permiso_rechazado)
            NotificacionesUiEvent.AvisoMostrado -> aviso.value = null
            is NotificacionesUiEvent.ActivarCambiado -> guardar(activo = evento.activo)
            is NotificacionesUiEvent.HoraElegida ->
                guardar(hora = evento.hora, minuto = evento.minuto)
            NotificacionesUiEvent.UbicacionConcedida -> actualizarUbicacion()
            NotificacionesUiEvent.UbicacionRechazada ->
                aviso.value = textos.obtener(R.string.notif_aviso_ubicacion_rechazada)
        }
    }

    private fun guardar(
        activo: Boolean = uiState.value.activo,
        hora: Int = uiState.value.hora,
        minuto: Int = uiState.value.minuto,
    ) {
        viewModelScope.launch {
            try {
                configurarRecordatorio(PreferenciasRecordatorio(activo, hora, minuto))
                aviso.update {
                    if (activo) mensajeActivado(hora, minuto)
                    else textos.obtener(R.string.notif_aviso_desactivado)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                aviso.value = textos.obtener(R.string.notif_aviso_error_guardar)
            }
        }
    }


    private fun mensajeActivado(hora: Int, minuto: Int): String {
        val ahora = LocalTime.now()
        val cuando = textos.obtener(
            if (ahora.isBefore(LocalTime.of(hora, minuto))) R.string.notif_hoy else R.string.notif_manana
        )
        return textos.obtener(
            R.string.notif_aviso_activado,
            cuando, hora, minuto, ahora.hour, ahora.minute
        )
    }

    private fun actualizarUbicacion() {
        viewModelScope.launch {
            try {
                if (obtenerUbicacion() == null) {
                    aviso.value = textos.obtener(R.string.error_ubicacion_gps)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                aviso.value = textos.obtener(R.string.error_ubicacion)
            }
        }
    }
}