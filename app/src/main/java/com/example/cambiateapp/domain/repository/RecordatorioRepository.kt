package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import kotlinx.coroutines.flow.Flow

interface RecordatorioRepository {
    fun observarPreferencias(): Flow<PreferenciasRecordatorio>
    suspend fun guardar(preferencias: PreferenciasRecordatorio)
}