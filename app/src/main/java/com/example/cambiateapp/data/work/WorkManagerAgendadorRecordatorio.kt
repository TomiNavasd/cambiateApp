package com.example.cambiateapp.data.work

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerAgendadorRecordatorio @Inject constructor(
    @ApplicationContext private val context: Context,
) : AgendadorRecordatorio {

    override fun programar(hora: Int, minuto: Int) {
        val espera = milisHastaProximaHora(hora, minuto)

        // Sin restricción de red: el aviso sale a la hora aunque no haya internet
        // (si no se puede consultar el clima, el Worker manda un aviso general).
        val pedido = PeriodicWorkRequestBuilder<RecordatorioDiarioWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(espera, TimeUnit.MILLISECONDS)
            .build()

        // CANCEL_AND_REENQUEUE reinicia el calendario: con UPDATE, cambiar la hora no movía el aviso.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOMBRE_TRABAJO,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            pedido,
        )

        Log.i(TAG, "Recordatorio programado para las %02d:%02d, en %d min (zona %s)".format(
            hora, minuto, TimeUnit.MILLISECONDS.toMinutes(espera), ZonedDateTime.now().zone
        ))
    }

    override fun cancelar() {
        WorkManager.getInstance(context).cancelUniqueWork(NOMBRE_TRABAJO)
        Log.i(TAG, "Recordatorio cancelado")
    }

    /** Milisegundos hasta la próxima vez que el reloj del dispositivo marque hora:minuto:00. */
    private fun milisHastaProximaHora(hora: Int, minuto: Int): Long {
        val ahora = ZonedDateTime.now()
        var proximo = ahora.toLocalDate().atTime(hora, minuto).atZone(ahora.zone)
        if (!proximo.isAfter(ahora)) proximo = proximo.plusDays(1)
        return Duration.between(ahora, proximo).toMillis()
    }

    private companion object {
        const val NOMBRE_TRABAJO = "recordatorio_diario_clima"
        const val TAG = "Recordatorio"
    }
}