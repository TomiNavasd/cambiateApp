package com.example.cambiateapp.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import com.example.cambiateapp.domain.usecase.ObtenerConsejoDeMananaUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.coroutines.cancellation.CancellationException

@HiltWorker
class RecordatorioDiarioWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parametros: WorkerParameters,
    private val obtenerConsejoDeManana: ObtenerConsejoDeMananaUseCase,
    private val notificador: NotificadorConsejo,
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result = try {
        obtenerConsejoDeManana()?.let { notificador.mostrar(it) }
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (runAttemptCount < MAX_REINTENTOS) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_REINTENTOS = 3
    }
}