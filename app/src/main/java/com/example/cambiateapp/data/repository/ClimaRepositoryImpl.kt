package com.example.cambiateapp.data.repository

import com.example.cambiateapp.data.mapper.aPronosticoDeManana
import com.example.cambiateapp.data.preferences.ClimaCache
import com.example.cambiateapp.data.preferences.ClimaLocalDataSource
import com.example.cambiateapp.data.remote.OpenMeteoApi
import com.example.cambiateapp.domain.model.ClimaNoDisponibleException
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.domain.repository.ClimaRepository
import com.google.gson.JsonParseException
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs
import retrofit2.HttpException

class ClimaRepositoryImpl(
    private val api: OpenMeteoApi,
    private val cache: ClimaCache? = null,
    private val ahora: () -> Long = System::currentTimeMillis,
) : ClimaRepository {

    @Inject
    constructor(api: OpenMeteoApi, cache: ClimaLocalDataSource) :
            this(api, cache as ClimaCache?, System::currentTimeMillis)

    override suspend fun pronosticoDeManana(coordenadas: Coordenadas): Pronostico {
        val respuesta = try {
            api.pronostico(coordenadas.latitud, coordenadas.longitud)
        } catch (e: IOException) {
            return desdeCache(coordenadas)
                ?: throw ClimaNoDisponibleException("No hay conexión para consultar el clima", e)
        } catch (e: HttpException) {
            return desdeCache(coordenadas)
                ?: throw ClimaNoDisponibleException("El servicio del clima respondió con error ${e.code()}", e)
        } catch (e: JsonParseException) {
            return desdeCache(coordenadas)
                ?: throw ClimaNoDisponibleException("No pudimos interpretar la respuesta del clima", e)
        }

        val pronostico = respuesta.aPronosticoDeManana().copy(obtenidoEnMs = ahora(), deCache = false)
        guardarEnCache(coordenadas, pronostico)
        return pronostico
    }

    private suspend fun guardarEnCache(coordenadas: Coordenadas, pronostico: Pronostico) {
        try {
            cache?.guardar(coordenadas, pronostico)
        } catch (e: IOException) {
            // sin cache esta vez; no afecta al usuario
        }
    }

    private suspend fun desdeCache(coordenadas: Coordenadas): Pronostico? {
        val guardado = try {
            cache?.leer()
        } catch (e: IOException) {
            null
        }
        if (guardado == null) return null

        val obtenidoEn = guardado.pronostico.obtenidoEnMs ?: return null
        val edad = ahora() - obtenidoEn
        val cerca = abs(guardado.coordenadas.latitud - coordenadas.latitud) < MAX_DISTANCIA_GRADOS &&
                abs(guardado.coordenadas.longitud - coordenadas.longitud) < MAX_DISTANCIA_GRADOS

        return if (edad in 0..MAX_EDAD_MS && cerca) guardado.pronostico.copy(deCache = true) else null
    }

    private companion object {
        const val MAX_EDAD_MS = 12L * 60 * 60 * 1000

        const val MAX_DISTANCIA_GRADOS = 0.2
    }
}