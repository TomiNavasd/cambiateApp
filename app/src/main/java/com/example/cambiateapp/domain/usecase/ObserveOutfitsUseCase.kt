package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveOutfitsUseCase @Inject constructor(
    private val repository: OutfitRepository
) {
    operator fun invoke(): Flow<List<Outfit>> = repository.observarOutfits()
}