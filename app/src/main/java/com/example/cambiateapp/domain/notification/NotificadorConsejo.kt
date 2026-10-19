package com.example.cambiateapp.domain.notification

import com.example.cambiateapp.domain.model.ConsejoClima

interface NotificadorConsejo {
    fun mostrar(consejo: ConsejoClima): Boolean
}