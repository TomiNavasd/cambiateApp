package com.example.cambiateapp.di

import com.example.cambiateapp.presentation.common.ProveedorDeTextos
import com.example.cambiateapp.presentation.common.ProveedorDeTextosAndroid
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TextosModule {

    @Binds
    abstract fun bindProveedorDeTextos(impl: ProveedorDeTextosAndroid): ProveedorDeTextos
}