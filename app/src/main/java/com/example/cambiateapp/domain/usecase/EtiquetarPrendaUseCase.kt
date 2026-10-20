package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.VocabularioEtiquetas
import com.example.cambiateapp.domain.repository.AnalizadorDePrenda
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pide a la IA las etiquetas de una prenda. Es "mejor esfuerzo": si algo falla o tarda demasiado
 * devuelve una lista vacía, porque etiquetar es un extra y nunca debe impedir guardar la prenda.
 */
class EtiquetarPrendaUseCase @Inject constructor(
    private val analizador: AnalizadorDePrenda
) {
    suspend operator fun invoke(fotoUrl: String, categoria: Categoria): List<String> {
        if (fotoUrl.isBlank()) return emptyList()

        val crudas = withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
            analizador.analizar(fotoUrl, categoria).getOrNull()
        } ?: return emptyList()

        return crudas
            .map { it.trim().lowercase() }
            .filter { it in VocabularioEtiquetas.PERMITIDAS } // descarta lo que la IA invente
            .distinct()
            .take(VocabularioEtiquetas.MAXIMO_POR_PRENDA)
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 20_000L
    }
}