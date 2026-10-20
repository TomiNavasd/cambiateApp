package com.example.cambiateapp.di

import com.example.cambiateapp.data.remote.OpenMeteoApi
import com.example.cambiateapp.data.repository.ClimaRepositoryImpl
import com.example.cambiateapp.domain.repository.ClimaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Cómo se arma Retrofit: se crea una sola vez para toda la app. */
@Module
@InstallIn(SingletonComponent::class)
object ClimaApiModule {

    @Provides
    @Singleton
    fun provideOpenMeteoApi(): OpenMeteoApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}

/** Cuando alguien pide un ClimaRepository, Hilt entrega el real. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ClimaModule {

    @Binds
    abstract fun bindClimaRepository(impl: ClimaRepositoryImpl): ClimaRepository
}