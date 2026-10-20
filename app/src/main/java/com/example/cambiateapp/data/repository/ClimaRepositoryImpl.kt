package com.example.cambiateapp.data.repository

import com.example.cambiateapp.data.mapper.aPronosticoDeManana
import com.example.cambiateapp.data.remote.OpenMeteoApi
import com.example.cambiateapp.domain.model.ClimaNoDisponibleException
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.model.Pronostico
import com.example.cambiateapp.domain.repository.ClimaRepository
import com.google.gson.JsonParseException
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException

class ClimaRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
) : ClimaRepository {

    override suspend fun pronosticoDeManana(coordenadas: Coordenadas): Pronostico {
        val respuesta = try {
            api.pronostico(coordenadas.latitud, coordenadas.longitud)
        } catch (e: IOException) {
            throw ClimaNoDisponibleException("No hay conexión para consultar el clima", e)
        } catch (e: HttpException) {
            throw ClimaNoDisponibleException("El servicio del clima respondió con error ${e.code()}", e)
        } catch (e: JsonParseException) {
            throw ClimaNoDisponibleException("No pudimos interpretar la respuesta del clima", e)
        }
        return respuesta.aPronosticoDeManana()
    }
}