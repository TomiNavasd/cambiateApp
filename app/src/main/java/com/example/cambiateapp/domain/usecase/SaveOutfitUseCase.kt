package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitRepository
import javax.inject.Inject

class SaveOutfitUseCase @Inject constructor(
    private val repository: OutfitRepository
) {
    suspend operator fun invoke(outfit: Outfit): Result<Unit> {
        if (outfit.prendasIds.isEmpty()) {
            return Result.failure(DomainException.Validacion(CampoInvalido.PRENDAS))
        }
        return repository.guardarOutfit(outfit)
    }
}