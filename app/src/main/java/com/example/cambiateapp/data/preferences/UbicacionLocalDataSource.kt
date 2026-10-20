package com.example.cambiateapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.cambiateapp.domain.model.Coordenadas
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Archivo donde el celular guarda la última ubicación (separado del de recordatorios). */
private val Context.ubicacionDataStore by preferencesDataStore(name = "ubicacion_prefs")

/** Guarda y lee la última ubicación conocida del usuario. */
@Singleton
class UbicacionLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun guardar(coordenadas: Coordenadas) {
        context.ubicacionDataStore.edit { preferencias ->
            preferencias[LATITUD] = coordenadas.latitud
            preferencias[LONGITUD] = coordenadas.longitud
        }
    }

    /** Devuelve null si nunca se guardó una ubicación. */
    suspend fun leer(): Coordenadas? {
        val preferencias = context.ubicacionDataStore.data.first()
        val latitud = preferencias[LATITUD] ?: return null
        val longitud = preferencias[LONGITUD] ?: return null
        return Coordenadas(latitud = latitud, longitud = longitud)
    }

    private companion object {
        val LATITUD = doublePreferencesKey("latitud")
        val LONGITUD = doublePreferencesKey("longitud")
    }
}