package com.example.cambiateapp.data.repository

import com.example.cambiateapp.data.mapper.toDomain
import com.example.cambiateapp.data.mapper.toDomainException
import com.example.cambiateapp.data.mapper.toDto
import com.example.cambiateapp.data.remote.OutfitDto
import com.example.cambiateapp.data.remote.llamarRemoto
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class OutfitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OutfitRepository {

    private val outfits get() = firestore.collection(COLECCION)

    override fun observarOutfits(): Flow<List<Outfit>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            close(DomainException.SesionNoIniciada)
            return@callbackFlow
        }

        val registro = outfits
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toDomainException())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toObject(OutfitDto::class.java)?.toDomain() }
                    .sortedByDescending { it.fecha } // se ordena acá para no necesitar un índice
                trySend(lista)
            }

        awaitClose { registro.remove() }
    }

    override suspend fun guardarOutfit(outfit: Outfit): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(DomainException.SesionNoIniciada)
        return try {
            llamarRemoto {
                val dto = outfit.toDto(uid)
                if (outfit.esNuevo) {
                    outfits.add(dto).await()
                } else {
                    outfits.document(outfit.id)
                        .set(dto, SetOptions.mergeFields(CAMPOS_EDITABLES))
                        .await()
                }
            }
            Result.success(Unit)
        } catch (e: DomainException) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarOutfit(id: String): Result<Unit> {
        if (auth.currentUser == null) return Result.failure(DomainException.SesionNoIniciada)
        return try {
            llamarRemoto { outfits.document(id).delete().await() }
            Result.success(Unit)
        } catch (e: DomainException) {
            Result.failure(e)
        }
    }

    private companion object {
        const val COLECCION = "outfits"
        val CAMPOS_EDITABLES = listOf("ocasion", "prendasIds", "favorito")
    }
}