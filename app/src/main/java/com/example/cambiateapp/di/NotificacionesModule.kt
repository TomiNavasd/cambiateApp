package com.example.cambiateapp.di

import com.example.cambiateapp.data.notification.AndroidNotificadorConsejo
import com.example.cambiateapp.domain.notification.NotificadorConsejo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificacionesModule {

    @Binds
    abstract fun bindNotificador(impl: AndroidNotificadorConsejo): NotificadorConsejo
}