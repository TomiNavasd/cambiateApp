package com.example.cambiateapp.di

import com.example.cambiateapp.data.preferences.TemaPreferencesRepository
import com.example.cambiateapp.domain.repository.TemaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Cuando alguien pide un TemaRepository, Hilt entrega el que guarda en DataStore. */
@Module
@InstallIn(SingletonComponent::class)
abstract class TemaModule {

    @Binds
    abstract fun bindTemaRepository(impl: TemaPreferencesRepository): TemaRepository
}