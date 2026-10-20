package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.VocabularioEtiquetas
import com.example.cambiateapp.domain.repository.AnalizadorDePrenda
import javax.inject.Inject

/**
 * Pide a la IA las etiquetas de una prenda. Es "mejor esfuerzo": si algo falla devuelve una
 * lista vacía, porque etiquetar es un extra y nunca debe impedir guardar la prenda.
 */
class EtiquetarPrendaUseCase @Inject constructor(
    private val analizador: AnalizadorDePrenda
) {
    suspend operator fun invoke(fotoUrl: String, categoria: Categoria): List<String> {
        if (fotoUrl.isBlank()) return emptyList()
        return analizador.analizar(fotoUrl, categoria)
            .getOrElse { return emptyList() }
            .map { it.trim().lowercase() }
            .filter { it in VocabularioEtiquetas.PERMITIDAS } // descarta lo que la IA invente
            .distinct()
            .take(VocabularioEtiquetas.MAXIMO_POR_PRENDA)
    }
}