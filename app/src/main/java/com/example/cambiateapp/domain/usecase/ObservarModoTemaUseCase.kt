package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.repository.TemaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservarModoTemaUseCase @Inject constructor(
    private val repositorio: TemaRepository,
) {
    operator fun invoke(): Flow<ModoTema> = repositorio.observarModo()
}