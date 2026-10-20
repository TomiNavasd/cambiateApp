package com.example.cambiateapp.data.ai

import android.util.Log
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.OutfitSuggester
import javax.inject.Inject


class SugeridorConRespaldo(
    private val principal: OutfitSuggester,
    private val respaldo: OutfitSuggester
) : OutfitSuggester {

    @Inject
    constructor(
        claude: ClaudeOutfitSuggester,
        reglas: ReglasOutfitSuggester
    ) : this(principal = claude, respaldo = reglas)

    override suspend fun sugerir(ocasion: Ocasion, prendas: List<Prenda>): Result<List<Outfit>> {
        val deClaude = principal.sugerir(ocasion, prendas)
        if (!deClaude.getOrNull().isNullOrEmpty()) return deClaude

        deClaude.exceptionOrNull()?.let { avisarFallo(it) }

        return respaldo.sugerir(ocasion, prendas)
    }

    private fun avisarFallo(error: Throwable) {
        runCatching { Log.w(TAG, "Claude falló, uso las reglas simples de respaldo", error) }
    }

    private companion object {
        const val TAG = "SugeridorOutfits"
    }
}