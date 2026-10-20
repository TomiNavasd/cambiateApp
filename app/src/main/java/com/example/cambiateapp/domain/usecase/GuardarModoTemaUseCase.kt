package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.repository.TemaRepository
import javax.inject.Inject

class GuardarModoTemaUseCase @Inject constructor(
    private val repositorio: TemaRepository,
) {
    suspend operator fun invoke(modo: ModoTema) = repositorio.guardarModo(modo)
}