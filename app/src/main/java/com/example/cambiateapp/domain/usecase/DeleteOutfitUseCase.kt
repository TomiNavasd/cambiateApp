package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.repository.OutfitRepository
import javax.inject.Inject

class DeleteOutfitUseCase @Inject constructor(
    private val repository: OutfitRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.eliminarOutfit(id)
}