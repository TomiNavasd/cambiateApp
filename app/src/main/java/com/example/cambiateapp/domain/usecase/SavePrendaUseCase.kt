package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import javax.inject.Inject

class SavePrendaUseCase @Inject constructor(
    private val repository: PrendaRepository
) {
    suspend operator fun invoke(prenda: Prenda): Result<Unit> {
        val nombre = prenda.nombre.trim()
        if (nombre.isEmpty()) {
            return Result.failure(DomainException.Validacion(CampoInvalido.NOMBRE))
        }
        if (prenda.fotoUrl.isBlank()) {
            return Result.failure(DomainException.Validacion(CampoInvalido.FOTO))
        }
        return repository.guardarPrenda(prenda.copy(nombre = nombre))
    }
}