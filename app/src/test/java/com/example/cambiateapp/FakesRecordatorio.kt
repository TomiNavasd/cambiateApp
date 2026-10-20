package com.example.cambiateapp

import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import com.example.cambiateapp.domain.repository.UbicacionRepository
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

/** Una ubicación de mentira: devuelve lo que le pidamos y cuenta cuántas veces la consultaron. */
class UbicacionRepositoryFake(
    var ubicacion: Coordenadas? = Coordenadas(latitud = -31.42, longitud = -64.19),
) : UbicacionRepository {
    var consultas = 0
    var fallar = false

    override suspend fun ubicacionActual(): Coordenadas? {
        consultas++
        if (fallar) throw IllegalStateException("sin GPS")
        return ubicacion
    }

    override suspend fun ultimaUbicacionConocida(): Coordenadas? = ubicacion
}