package com.example.cambiateapp.di

import com.example.cambiateapp.data.preferences.RecordatorioPreferencesRepository
import com.example.cambiateapp.data.work.WorkManagerAgendadorRecordatorio
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RecordatorioModule {

    @Binds
    abstract fun bindRecordatorioRepository(impl: RecordatorioPreferencesRepository): RecordatorioRepository

    @Binds
    abstract fun bindAgendador(impl: WorkManagerAgendadorRecordatorio): AgendadorRecordatorio
}