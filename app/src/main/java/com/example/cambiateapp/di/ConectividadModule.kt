package com.example.cambiateapp.di

import com.example.cambiateapp.data.repository.ConectividadRepositoryImpl
import com.example.cambiateapp.domain.repository.ConectividadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ConectividadModule {

    @Binds
    abstract fun bindConectividadRepository(impl: ConectividadRepositoryImpl): ConectividadRepository
}