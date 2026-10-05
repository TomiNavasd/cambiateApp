package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePrendasUseCase @Inject constructor(
    private val repository: PrendaRepository
) {
    operator fun invoke(): Flow<List<Prenda>> = repository.observarPrendas()
}