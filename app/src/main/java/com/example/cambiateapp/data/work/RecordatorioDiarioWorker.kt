package com.example.cambiateapp.data.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import com.example.cambiateapp.domain.usecase.ObtenerConsejoDeMananaUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

@HiltWorker
class RecordatorioDiarioWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parametros: WorkerParameters,
    private val obtenerConsejoDeManana: ObtenerConsejoDeMananaUseCase,
    private val notificador: NotificadorConsejo,
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "El recordatorio se está ejecutando")

        val consejo = try {
            withTimeoutOrNull(TIEMPO_MAXIMO_CLIMA_MS) { obtenerConsejoDeManana() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo armar el consejo del clima", e)
            null
        }

        // Pase lo que pase con el clima, el aviso sale: nunca queda en silencio.
        val mostrada = notificador.mostrar(consejo ?: CONSEJO_GENERAL)
        Log.i(TAG, if (mostrada) "Notificación mostrada" else "No hay permiso de notificaciones")

        return Result.success()
    }

    private companion object {
        const val TAG = "Recordatorio"
        const val TIEMPO_MAXIMO_CLIMA_MS = 20_000L

        val CONSEJO_GENERAL = ConsejoClima(
            titulo = "Mirá el clima de mañana",
            mensaje = "Abrí CambiateApp y fijate qué ponerte según el pronóstico.",
            prendasSugeridas = emptyList(),
        )
    }
}