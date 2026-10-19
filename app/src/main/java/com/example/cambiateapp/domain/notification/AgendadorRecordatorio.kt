package com.example.cambiateapp.domain.notification

interface AgendadorRecordatorio {
    fun programar(hora: Int, minuto: Int)
    fun cancelar()
}