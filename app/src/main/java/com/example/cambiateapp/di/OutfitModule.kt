package com.example.cambiateapp.di

import com.example.cambiateapp.data.repository.OutfitRepositoryImpl
import com.example.cambiateapp.domain.repository.OutfitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OutfitModule {

    @Binds
    abstract fun bindOutfitRepository(impl: OutfitRepositoryImpl): OutfitRepository
}