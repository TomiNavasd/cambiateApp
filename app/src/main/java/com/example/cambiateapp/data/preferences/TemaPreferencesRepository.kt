package com.example.cambiateapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.repository.TemaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Archivo donde el celular guarda el tema elegido (uno propio, distinto al de recordatorios). */
private val Context.temaDataStore by preferencesDataStore(name = "tema_prefs")

@Singleton
class TemaPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : TemaRepository {

    override fun observarModo(): Flow<ModoTema> =
        context.temaDataStore.data
            .catch { error ->
                // Si no se puede leer el archivo, usamos el valor por defecto.
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferencias ->
                val guardado = preferencias[MODO]
                ModoTema.entries.firstOrNull { it.name == guardado } ?: ModoTema.SISTEMA
            }

    override suspend fun guardarModo(modo: ModoTema) {
        context.temaDataStore.edit { preferencias -> preferencias[MODO] = modo.name }
    }

    private companion object {
        val MODO = stringPreferencesKey("modo")
    }
}