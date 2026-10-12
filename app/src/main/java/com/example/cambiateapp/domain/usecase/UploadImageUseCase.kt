package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.repository.ImageStorageRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val repository: ImageStorageRepository
) {
    /** Sube la imagen y devuelve la URL pública. */
    suspend operator fun invoke(uri: String): Result<String> = repository.subirImagen(uri)
}