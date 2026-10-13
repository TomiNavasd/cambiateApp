package com.example.cambiateapp.data.repository

import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class FakeOutfitRepository @Inject constructor() : OutfitRepository {

    private val outfits = MutableStateFlow<List<Outfit>>(emptyList())

    override fun observarOutfits(): Flow<List<Outfit>> = outfits

    override suspend fun guardarOutfit(outfit: Outfit): Result<Unit> {
        delay(300)
        outfits.update { actuales ->
            if (outfit.esNuevo) {
                listOf(outfit.copy(id = UUID.randomUUID().toString())) + actuales
            } else {
                actuales.map { if (it.id == outfit.id) outfit else it }
            }
        }
        return Result.success(Unit)
    }

    override suspend fun eliminarOutfit(id: String): Result<Unit> {
        delay(300)
        outfits.update { actuales -> actuales.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}