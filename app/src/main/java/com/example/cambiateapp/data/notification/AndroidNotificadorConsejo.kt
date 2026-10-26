package com.example.cambiateapp.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cambiateapp.DeepLinks
import com.example.cambiateapp.MainActivity
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import com.example.cambiateapp.ui.text.mensajeLocalizado
import com.example.cambiateapp.ui.text.tituloLocalizado
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNotificadorConsejo @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificadorConsejo {

    @SuppressLint("MissingPermission") // el permiso se verifica justo antes de notificar
    override fun mostrar(consejo: ConsejoClima): Boolean {
        if (!tienePermiso()) return false

        crearCanal()

        val mensaje = consejo.mensajeLocalizado(context)
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(consejo.tituloLocalizado(context))
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intentAbrirOutfits())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion)
        return true
    }

    /** Al tocar la notificación se abre la app directamente en la pantalla Outfits. */
    private fun intentAbrirOutfits(): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(DeepLinks.OUTFITS),
            context,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.notif_canal_nombre),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notif_canal_descripcion)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        // Volver a crearlo con el mismo id actualiza el nombre al idioma actual.
        manager.createNotificationChannel(canal)
    }

    private companion object {
        const val CANAL_ID = "consejos_clima"
        const val ID_NOTIFICACION = 1001
    }
}