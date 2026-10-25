package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.repository.ConectividadRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservarConectividadUseCase @Inject constructor(
    private val repositorio: ConectividadRepository
) {
    operator fun invoke(): Flow<Boolean> = repositorio.hayConexion
}