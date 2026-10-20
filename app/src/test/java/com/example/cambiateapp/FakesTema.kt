package com.example.cambiateapp

import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.repository.TemaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Repositorio de tema en memoria: sirve para probar sin DataStore. */
class TemaRepositoryFake(inicial: ModoTema = ModoTema.SISTEMA) : TemaRepository {

    private val modo = MutableStateFlow(inicial)

    /** Si es true, guardar falla, para probar el camino de error. */
    var fallarAlGuardar = false

    override fun observarModo(): Flow<ModoTema> = modo

    override suspend fun guardarModo(modo: ModoTema) {
        if (fallarAlGuardar) throw java.io.IOException("Disco lleno")
        this.modo.value = modo
    }
}