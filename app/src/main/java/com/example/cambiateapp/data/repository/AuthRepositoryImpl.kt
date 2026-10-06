package com.example.cambiateapp.data.repository

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.User
import com.example.cambiateapp.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val usuarioActual: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            trySend(firebaseUser?.let { User(id = it.uid, email = it.email.orEmpty()) })
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val resultado = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = resultado.user
                ?: return Result.failure(DomainException.Desconocido())
            Result.success(User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(traducirError(e))
        }
    }

    override suspend fun registrar(email: String, pass: String): Result<User> {
        return try {
            val resultado = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = resultado.user
                ?: return Result.failure(DomainException.Desconocido())
            Result.success(User(id = firebaseUser.uid, email = firebaseUser.email.orEmpty()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val error = when (e) {
                is FirebaseAuthWeakPasswordException -> DomainException.ContrasenaDebil
                is FirebaseAuthInvalidCredentialsException -> DomainException.EmailInvalido
                else -> traducirError(e)
            }
            Result.failure(error)
        }
    }

    override fun cerrarSesion() {
        firebaseAuth.signOut()
    }

    private fun traducirError(e: Exception): DomainException = when (e) {
        is FirebaseAuthWeakPasswordException -> DomainException.ContrasenaDebil
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> DomainException.CredencialesInvalidas
        is FirebaseAuthUserCollisionException -> DomainException.EmailYaRegistrado
        is FirebaseNetworkException -> DomainException.SinConexion
        else -> DomainException.Desconocido(e)
    }
}