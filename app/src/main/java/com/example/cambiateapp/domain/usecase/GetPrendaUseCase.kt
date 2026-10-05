package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import javax.inject.Inject

class GetPrendaUseCase @Inject constructor(
    private val repository: PrendaRepository
) {
    suspend operator fun invoke(id: String): Prenda? = repository.obtenerPrenda(id)
}