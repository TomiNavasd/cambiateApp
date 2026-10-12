package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Outfit
import kotlinx.coroutines.flow.Flow

interface OutfitRepository {
    fun observarOutfits(): Flow<List<Outfit>>

    suspend fun guardarOutfit(outfit: Outfit): Result<Unit>

    suspend fun eliminarOutfit(id: String): Result<Unit>
}