package com.example.cambiateapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.climaDataStore by preferencesDataStore(name = "clima_prefs")

@Singleton
class ClimaLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClimaCache {

    override suspend fun guardar(coordenadas: Coordenadas, pronostico: Pronostico) {
        val obtenidoEn = pronostico.obtenidoEnMs ?: return
        context.climaDataStore.edit { p ->
            p[LATITUD] = coordenadas.latitud
            p[LONGITUD] = coordenadas.longitud
            p[TEMP_MIN] = pronostico.tempMinC
            p[TEMP_MAX] = pronostico.tempMaxC
            p[LLUVIA] = pronostico.probLluviaPct
            p[OBTENIDO_EN] = obtenidoEn
        }
    }

    override suspend fun leer(): ClimaGuardado? {
        val p = context.climaDataStore.data.first()
        return ClimaGuardado(
            coordenadas = Coordenadas(p[LATITUD] ?: return null, p[LONGITUD] ?: return null),
            pronostico = Pronostico(
                tempMinC = p[TEMP_MIN] ?: return null,
                tempMaxC = p[TEMP_MAX] ?: return null,
                probLluviaPct = p[LLUVIA] ?: return null,
                obtenidoEnMs = p[OBTENIDO_EN] ?: return null,
            ),
        )
    }

    private companion object {
        val LATITUD = doublePreferencesKey("latitud")
        val LONGITUD = doublePreferencesKey("longitud")
        val TEMP_MIN = doublePreferencesKey("temp_min")
        val TEMP_MAX = doublePreferencesKey("temp_max")
        val LLUVIA = intPreferencesKey("lluvia_pct")
        val OBTENIDO_EN = longPreferencesKey("obtenido_en_ms")
    }
}