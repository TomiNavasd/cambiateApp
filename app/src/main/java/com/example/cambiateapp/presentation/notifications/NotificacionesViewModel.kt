package com.example.cambiateapp.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.usecase.ConfigurarRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.EnviarNotificacionDePruebaUseCase
import com.example.cambiateapp.domain.usecase.ObservarPreferenciasRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.ObtenerUbicacionUseCase
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
                aviso.value = if (mostrada) {
                    "Listo, mirá la barra de notificaciones"
                } else {
                    "No hay permiso para mostrar notificaciones"
                }
            }
            NotificacionesUiEvent.PermisoRechazado ->
                aviso.value = "Sin permiso no podemos avisarte. Activalo en los ajustes del sistema."
            NotificacionesUiEvent.AvisoMostrado -> aviso.value = null
            is NotificacionesUiEvent.ActivarCambiado -> guardar(activo = evento.activo)
            is NotificacionesUiEvent.HoraElegida ->
                guardar(hora = evento.hora, minuto = evento.minuto)
            NotificacionesUiEvent.UbicacionConcedida -> actualizarUbicacion()
            NotificacionesUiEvent.UbicacionRechazada ->
                aviso.value = "Sin tu ubicación no podemos saber el clima de tu zona. Activala en los ajustes del sistema."
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
                    if (activo) mensajeActivado(hora, minuto) else "Recordatorio desactivado"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                aviso.value = "No pudimos guardar el recordatorio"
            }
        }
    }

    /**
     * Dice cuándo suena la primera vez según el reloj del dispositivo, así si el reloj
     * está corrido (por ejemplo, un emulador en otra zona horaria) se nota enseguida.
     */
    private fun mensajeActivado(hora: Int, minuto: Int): String {
        val ahora = LocalTime.now()
        val cuando = if (ahora.isBefore(LocalTime.of(hora, minuto))) "hoy" else "mañana"
        return "Recordatorio activado: te avisamos $cuando a las %02d:%02d y después todos los días. "
            .format(hora, minuto) +
                "Ahora son %02d:%02d en este dispositivo.".format(ahora.hour, ahora.minute)
    }

    /** Pide la ubicación actual. Al obtenerla, queda guardada para el aviso en segundo plano. */
    private fun actualizarUbicacion() {
        viewModelScope.launch {
            try {
                if (obtenerUbicacion() == null) {
                    aviso.value = "No pudimos obtener tu ubicación. Revisá que el GPS esté activado."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                aviso.value = "No pudimos obtener tu ubicación."
            }
        }
    }
}