package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.TipoConsejo
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import javax.inject.Inject

class EnviarNotificacionDePruebaUseCase @Inject constructor(
    private val notificador: NotificadorConsejo,
) {
    operator fun invoke(): Boolean = notificador.mostrar(
        ConsejoClima(
            titulo = "Mañana llueve (prueba)",
            mensaje = "Así se va a ver tu aviso del clima. Tocá para ver tus outfits.",
            prendasSugeridas = emptyList(),
            tipo = TipoConsejo.Prueba,
        )
    )
}