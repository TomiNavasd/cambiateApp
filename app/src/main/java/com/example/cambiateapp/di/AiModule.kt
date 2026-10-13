package com.example.cambiateapp.di

import com.example.cambiateapp.data.ai.SugeridorConRespaldo
import com.example.cambiateapp.domain.repository.OutfitSuggester
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    abstract fun bindOutfitSuggester(impl: SugeridorConRespaldo): OutfitSuggester
}