package com.example.cambiateapp.data.mapper

import com.example.cambiateapp.domain.error.DomainException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

fun Throwable.toDomainException(): DomainException = when (this) {
    is DomainException -> this
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> DomainException.SinPermisos
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DomainException.SesionNoIniciada
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> DomainException.SinConexion
        else -> DomainException.Desconocido(this)
    }
    is FirebaseNetworkException -> DomainException.SinConexion
    else -> DomainException.Desconocido(this)
}