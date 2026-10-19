package com.example.cambiateapp

import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class RecordatorioRepositoryFake : RecordatorioRepository {
    val estado = MutableStateFlow(PreferenciasRecordatorio())

    override fun observarPreferencias(): Flow<PreferenciasRecordatorio> = estado

    override suspend fun guardar(preferencias: PreferenciasRecordatorio) {
        estado.value = preferencias
    }
}

class AgendadorFake : AgendadorRecordatorio {
    var programado: Pair<Int, Int>? = null
    var cancelaciones = 0

    override fun programar(hora: Int, minuto: Int) {
        programado = hora to minuto
    }

    override fun cancelar() {
        cancelaciones++
    }
}

class NotificadorFake(var conPermiso: Boolean = true) : NotificadorConsejo {
    var ultimoConsejo: ConsejoClima? = null

    override fun mostrar(consejo: ConsejoClima): Boolean {
        if (conPermiso) ultimoConsejo = consejo
        return conPermiso
    }
}