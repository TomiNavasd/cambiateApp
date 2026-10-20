package com.example.cambiateapp.di

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.repository.UbicacionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * TEMPORAL: ubicación falsa (Buenos Aires) hasta que esté la real (tarea T3).
 * El clima ya es real: ver ClimaModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object TempClimaUbicacionModule {

    @Provides
    @Singleton
    fun provideUbicacion(): UbicacionRepository = object : UbicacionRepository {
        private val buenosAires = Coordenadas(latitud = -34.6, longitud = -58.4)
        override suspend fun ubicacionActual(): Coordenadas = buenosAires
        override suspend fun ultimaUbicacionConocida(): Coordenadas = buenosAires
    }
}