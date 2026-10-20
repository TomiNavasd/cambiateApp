package com.example.cambiateapp.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.cambiateapp.data.preferences.UbicacionLocalDataSource
import com.example.cambiateapp.domain.model.Coordenadas
import com.example.cambiateapp.domain.repository.UbicacionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class UbicacionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cliente: FusedLocationProviderClient,
    private val local: UbicacionLocalDataSource,
) : UbicacionRepository {

    /** Pide la posición al GPS. Devuelve null si no hay permiso o no se pudo obtener. */
    override suspend fun ubicacionActual(): Coordenadas? {
        if (!tienePermiso()) return null

        val ubicacion = try {
            withTimeoutOrNull(TIEMPO_MAXIMO_MS) { pedirUbicacionAlGps() }
        } catch (e: SecurityException) {
            null
        } ?: return null

        val coordenadas = Coordenadas(latitud = ubicacion.latitude, longitud = ubicacion.longitude)
        local.guardar(coordenadas) // queda disponible para el recordatorio en segundo plano
        return coordenadas
    }

    /** Devuelve la última ubicación que guardamos, o null si todavía no hay ninguna. */
    override suspend fun ultimaUbicacionConocida(): Coordenadas? = local.leer()

    private fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /** El permiso ya se comprobó en ubicacionActual(). */
    @SuppressLint("MissingPermission")
    private suspend fun pedirUbicacionAlGps(): Location? =
        suspendCancellableCoroutine { continuacion ->
            val fuenteDeCancelacion = CancellationTokenSource()
            continuacion.invokeOnCancellation { fuenteDeCancelacion.cancel() }

            cliente.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                fuenteDeCancelacion.token,
            )
                .addOnSuccessListener { continuacion.resume(it) }
                .addOnFailureListener { continuacion.resume(null) }
        }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 15_000L
    }
}