package com.example.cambiateapp.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.cambiateapp.domain.repository.ConectividadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class ConectividadRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : ConectividadRepository {

    private val manager = context.getSystemService(ConnectivityManager::class.java)

    override val hayConexion: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                trySend(capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        trySend(estaConectado())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun estaConectado(): Boolean {
        val red = manager.activeNetwork ?: return false
        val capacidades = manager.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}