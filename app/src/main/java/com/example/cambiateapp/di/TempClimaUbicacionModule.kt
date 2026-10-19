package com.example.cambiateapp.di

import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.domain.repository.ClimaRepository
import com.example.cambiateapp.domain.repository.UbicacionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object TempClimaUbicacionModule {

    @Provides
    @Singleton
    fun provideClima(): ClimaRepository = object : ClimaRepository {
        override suspend fun pronosticoDeManana(coordenadas: Coordenadas) =
            Pronostico(tempMinC = 8.0, tempMaxC = 14.0, probLluviaPct = 70)
    }

    @Provides
    @Singleton
    fun provideUbicacion(): UbicacionRepository = object : UbicacionRepository {
        private val buenosAires = Coordenadas(latitud = -34.6, longitud = -58.4)
        override suspend fun ubicacionActual(): Coordenadas = buenosAires
        override suspend fun ultimaUbicacionConocida(): Coordenadas = buenosAires
    }
}