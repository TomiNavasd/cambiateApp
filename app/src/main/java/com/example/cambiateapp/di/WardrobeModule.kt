package com.example.cambiateapp.di

import com.example.cambiateapp.data.repository.FakePrendaRepository
import com.example.cambiateapp.domain.repository.PrendaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WardrobeModule {

    @Binds
    abstract fun bindPrendaRepository(impl: FakePrendaRepository): PrendaRepository
}