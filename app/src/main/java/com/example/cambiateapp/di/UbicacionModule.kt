package com.example.cambiateapp.di

import android.content.Context
import com.example.cambiateapp.data.repository.UbicacionRepositoryImpl
import com.example.cambiateapp.domain.repository.UbicacionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Cómo se arma el cliente de ubicación de Google: una sola vez para toda la app. */
@Module
@InstallIn(SingletonComponent::class)
object UbicacionApiModule {

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}

/** Cuando alguien pide un UbicacionRepository, Hilt entrega el real. */
@Module
@InstallIn(SingletonComponent::class)
abstract class UbicacionModule {

    @Binds
    abstract fun bindUbicacionRepository(impl: UbicacionRepositoryImpl): UbicacionRepository
}