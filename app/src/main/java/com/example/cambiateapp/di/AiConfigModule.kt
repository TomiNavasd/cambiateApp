package com.example.cambiateapp.di

import com.example.cambiateapp.BuildConfig
import com.example.cambiateapp.data.ai.CLAVE_ANTHROPIC
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AiConfigModule {

    @Provides
    @Named(CLAVE_ANTHROPIC)
    fun provideAnthropicApiKey(): String = BuildConfig.ANTHROPIC_API_KEY
}