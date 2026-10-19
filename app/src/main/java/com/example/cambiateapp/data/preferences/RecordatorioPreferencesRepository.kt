package com.example.cambiateapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.recordatorioDataStore by preferencesDataStore(name = "recordatorio_prefs")

@Singleton
class RecordatorioPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecordatorioRepository {

    private object Claves {
        val ACTIVO = booleanPreferencesKey("activo")
        val HORA = intPreferencesKey("hora")
        val MINUTO = intPreferencesKey("minuto")
    }

    override fun observarPreferencias(): Flow<PreferenciasRecordatorio> {
        val porDefecto = PreferenciasRecordatorio()
        return context.recordatorioDataStore.data.map { datos ->
            PreferenciasRecordatorio(
                activo = datos[Claves.ACTIVO] ?: porDefecto.activo,
                hora = datos[Claves.HORA] ?: porDefecto.hora,
                minuto = datos[Claves.MINUTO] ?: porDefecto.minuto,
            )
        }
    }

    override suspend fun guardar(preferencias: PreferenciasRecordatorio) {
        context.recordatorioDataStore.edit { datos ->
            datos[Claves.ACTIVO] = preferencias.activo
            datos[Claves.HORA] = preferencias.hora
            datos[Claves.MINUTO] = preferencias.minuto
        }
    }
}