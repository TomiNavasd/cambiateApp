package com.example.cambiateapp.domain.usecase

import javax.inject.Inject

class EtiquetarPrendaUseCase @Inject constructor(
    private val analizarPrenda: AnalizarPrendaUseCase
) {
    suspend operator fun invoke(fotoUrl: String): List<String> =
        analizarPrenda(fotoUrl)?.etiquetas.orEmpty()
}