package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.AnalisisPrenda
import com.example.cambiateapp.domain.model.VocabularioEtiquetas
import com.example.cambiateapp.domain.repository.AnalizadorDePrenda
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull


class AnalizarPrendaUseCase @Inject constructor(
    private val analizador: AnalizadorDePrenda
) {
    suspend operator fun invoke(fotoUrl: String): AnalisisPrenda? {
        if (fotoUrl.isBlank()) return null

        val cruda = withTimeoutOrNull(TIEMPO_MAXIMO_MS) {
            analizador.analizar(fotoUrl).getOrNull()
        } ?: return null

        return limpiar(cruda)
    }

    /** No confiamos en lo que devuelve la IA: recortamos textos y descartamos lo que no sirva. */
    private fun limpiar(cruda: AnalisisPrenda) = AnalisisPrenda(
        nombre = cruda.nombre.trim().take(MAX_NOMBRE),
        descripcion = cruda.descripcion.trim().take(MAX_DESCRIPCION),
        categoria = cruda.categoria,
        colores = cruda.colores
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX_COLORES),
        ocasiones = cruda.ocasiones.distinct(),
        etiquetas = cruda.etiquetas
            .map { it.trim().lowercase() }
            .filter { it in VocabularioEtiquetas.PERMITIDAS } // descarta lo que la IA invente
            .distinct()
            .take(VocabularioEtiquetas.MAXIMO_POR_PRENDA)
    )

    private companion object {
        const val TIEMPO_MAXIMO_MS = 20_000L
        const val MAX_NOMBRE = 40
        const val MAX_DESCRIPCION = 120
        const val MAX_COLORES = 4
    }
}