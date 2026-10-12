package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.repository.PrendaRepository
import javax.inject.Inject

class DeletePrendaUseCase @Inject constructor(
    private val repository: PrendaRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.eliminarPrenda(id)
}