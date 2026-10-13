package com.example.cambiateapp.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class OutfitDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val ocasion: String = "",
    val prendasIds: List<String> = emptyList(),
    val fecha: Timestamp? = null,
    val favorito: Boolean = false
)