package com.example.cambiateapp.di

import com.example.cambiateapp.data.repository.CloudinaryImageStorageRepository
import com.example.cambiateapp.domain.repository.ImageStorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    abstract fun bindImageStorageRepository(
            impl: CloudinaryImageStorageRepository
    ): ImageStorageRepository
}