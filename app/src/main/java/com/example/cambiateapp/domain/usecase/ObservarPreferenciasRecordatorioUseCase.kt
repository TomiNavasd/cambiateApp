package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservarPreferenciasRecordatorioUseCase @Inject constructor(
    private val repositorio: RecordatorioRepository,
) {
    operator fun invoke(): Flow<PreferenciasRecordatorio> = repositorio.observarPreferencias()
}