package com.example.cambiateapp.presentation.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

private const val TAG = "GoogleSignIn"

sealed interface ResultadoGoogle {
    data class Token(val idToken: String) : ResultadoGoogle
    data object Cancelado : ResultadoGoogle
    data class Error(val mensaje: String) : ResultadoGoogle
}


suspend fun pedirTokenDeGoogle(context: Context, webClientId: String): ResultadoGoogle {
    val opcion = GetSignInWithGoogleOption.Builder(webClientId).build()
    val pedido = GetCredentialRequest.Builder().addCredentialOption(opcion).build()

    return try {
        val respuesta = CredentialManager.create(context).getCredential(context, pedido)
        val credencial = respuesta.credential
        if (credencial is CustomCredential &&
            credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            ResultadoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
        } else {
            Log.w(TAG, "Tipo de credencial inesperado: ${credencial.type}")
            ResultadoGoogle.Error("No pudimos iniciar sesión con Google. Intentá de nuevo")
        }
    } catch (e: GetCredentialCancellationException) {
        // El usuario cerró el selector: no es un error, no mostramos nada.
        ResultadoGoogle.Cancelado
    } catch (e: NoCredentialException) {
        Log.w(TAG, "No hay cuentas de Google disponibles", e)
        ResultadoGoogle.Error("No encontramos una cuenta de Google en este celular")
    } catch (e: GetCredentialException) {
        Log.w(TAG, "Falló Credential Manager: ${e.type}", e)
        ResultadoGoogle.Error("No pudimos iniciar sesión con Google. Intentá de nuevo")
    } catch (e: GoogleIdTokenParsingException) {
        Log.w(TAG, "No se pudo leer el token de Google", e)
        ResultadoGoogle.Error("No pudimos iniciar sesión con Google. Intentá de nuevo")
    }
}