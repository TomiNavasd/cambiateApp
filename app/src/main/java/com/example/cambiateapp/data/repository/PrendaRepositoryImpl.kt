package com.example.cambiateapp.data.repository

import com.example.cambiateapp.data.mapper.toDomain
import com.example.cambiateapp.data.mapper.toDomainException
import com.example.cambiateapp.data.mapper.toDto
import com.example.cambiateapp.data.remote.PrendaDto
import com.example.cambiateapp.data.remote.confirmarOEncolar
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class PrendaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PrendaRepository {

    private val prendas get() = firestore.collection(COLECCION)

    override fun observarPrendas(): Flow<List<Prenda>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close(DomainException.SesionNoIniciada)
            return@callbackFlow
        }

        val registro = prendas
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toDomainException())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toObject(PrendaDto::class.java)?.toDomain() }
                    .sortedBy { it.nombre.lowercase() } // se ordena acá para no necesitar un índice en Firestore
                trySend(lista)
            }

        awaitClose { registro.remove() }
    }

    override suspend fun obtenerPrenda(id: String): Prenda? {
        val uid = auth.currentUser?.uid ?: throw DomainException.SesionNoIniciada
        return try {
            llamarRemoto {
                val dto = leerDocumento(id)?.toObject(PrendaDto::class.java)
                if (dto != null && dto.userId == uid) dto.toDomain() else null
            }
        } catch (e: DomainException.SinPermisos) {
            null // con nuestras reglas, "no existe" y "no es tuya" dan permiso denegado
        }
    }


    private suspend fun leerDocumento(id: String): DocumentSnapshot? {
        val referencia = prendas.document(id)
        val enCache = try {
            referencia.get(Source.CACHE).await()
        } catch (e: FirebaseFirestoreException) {
            null // no está en la copia local
        }
        if (enCache != null && enCache.exists()) return enCache
        return referencia.get().await()
    }

    override suspend fun guardarPrenda(prenda: Prenda): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(DomainException.SesionNoIniciada)
        return try {
            val dto = prenda.toDto(uid)
            if (prenda.esNueva) {
                prendas.add(dto).confirmarOEncolar()
            } else {
                prendas.document(prenda.id)
                    .set(dto, SetOptions.mergeFields(CAMPOS_EDITABLES))
                    .confirmarOEncolar()
            }
            Result.success(Unit)
        } catch (e: DomainException) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarPrenda(id: String): Result<Unit> {
        if (auth.currentUser == null) return Result.failure(DomainException.SesionNoIniciada)
        return try {
            prendas.document(id).delete().confirmarOEncolar()
            Result.success(Unit)
        } catch (e: DomainException) {
            Result.failure(e)
        }
    }

    private suspend fun <T> llamarRemoto(bloque: suspend () -> T): T =
        try {
            withTimeout(TIMEOUT_MS) { bloque() }
        } catch (e: TimeoutCancellationException) {
            throw DomainException.SinConexion
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e.toDomainException()
        }

    private companion object {
        const val COLECCION = "prendas"
        const val TIMEOUT_MS = 10_000L
        val CAMPOS_EDITABLES = listOf("nombre", "descripcion", "categoria", "colores", "ocasiones", "fotoUrl", "etiquetas")
    }
}