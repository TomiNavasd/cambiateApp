package com.example.cambiateapp.domain.repository

import com.example.cambiateapp.domain.model.Prenda
import kotlinx.coroutines.flow.Flow

interface PrendaRepository {
    /** Prendas del usuario logueado. Si falla, el Flow termina con una DomainException. */
    fun observarPrendas(): Flow<List<Prenda>>

    /** Devuelve null si la prenda no existe o no es del usuario logueado. */
    suspend fun obtenerPrenda(id: String): Prenda?

    /** Si prenda.esNueva la crea; si no, la actualiza. Falla con una DomainException. */
    suspend fun guardarPrenda(prenda: Prenda): Result<Unit>
}