package com.example.cambiateapp.domain.repository

interface ImageStorageRepository {
    suspend fun subirImagen(uri: String): Result<String>
}