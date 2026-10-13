package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitSuggester
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class SuggestOutfitsUseCase @Inject constructor(
    private val observePrendas: ObservePrendasUseCase,
    private val suggester: OutfitSuggester
) {
    suspend operator fun invoke(ocasion: Ocasion): Result<List<Outfit>> {
        val prendas = try {
            observePrendas().first()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return suggester.sugerir(ocasion, prendas)
    }
}