package com.example.cambiateapp.data.ai

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/** Ejecuta la llamada de OkHttp suspendiendo la corrutina; si se cancela, cancela también la red. */
internal suspend fun Call.await(): Response = suspendCancellableCoroutine { continuacion ->
    continuacion.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuacion.isActive) continuacion.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            continuacion.resume(response)
        }
    })
}