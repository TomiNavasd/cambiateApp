package com.example.cambiateapp.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerAgendadorRecordatorio @Inject constructor(
    @ApplicationContext private val context: Context,
) : AgendadorRecordatorio {

    override fun programar(hora: Int, minuto: Int) {
        val pedido = PeriodicWorkRequestBuilder<RecordatorioDiarioWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(minutosHastaProximaHora(hora, minuto), TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOMBRE_TRABAJO,
            ExistingPeriodicWorkPolicy.UPDATE,
            pedido,
        )
    }

    override fun cancelar() {
        WorkManager.getInstance(context).cancelUniqueWork(NOMBRE_TRABAJO)
    }

    private fun minutosHastaProximaHora(hora: Int, minuto: Int): Long {
        val ahora = LocalDateTime.now()
        var proximo = ahora.toLocalDate().atTime(hora, minuto)
        if (!proximo.isAfter(ahora)) proximo = proximo.plusDays(1)
        return Duration.between(ahora, proximo).toMinutes()
    }

    private companion object {
        const val NOMBRE_TRABAJO = "recordatorio_diario_clima"
    }
}