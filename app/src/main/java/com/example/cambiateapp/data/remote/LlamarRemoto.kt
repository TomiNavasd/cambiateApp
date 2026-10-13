package com.example.cambiateapp.data.remote

import com.example.cambiateapp.data.mapper.toDomainException
import com.example.cambiateapp.domain.error.DomainException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val TIMEOUT_REMOTO_MS = 10_000L

internal suspend fun <T> llamarRemoto(bloque: suspend () -> T): T =
    try {
        withTimeout(TIMEOUT_REMOTO_MS) { bloque() }
    } catch (e: TimeoutCancellationException) {
        throw DomainException.SinConexion
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toDomainException()
    }