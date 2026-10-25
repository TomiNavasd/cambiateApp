package com.example.cambiateapp.data.remote

import com.example.cambiateapp.data.mapper.toDomainException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException

private const val ESPERA_CONFIRMACION_MS = 4_000L
internal suspend fun <T> Task<T>.confirmarOEncolar() {
    try {
        withTimeout(ESPERA_CONFIRMACION_MS) { await() }
    } catch (e: TimeoutCancellationException) {
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toDomainException()
    }
}