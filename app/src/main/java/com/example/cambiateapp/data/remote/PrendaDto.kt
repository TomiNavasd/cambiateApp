package com.example.cambiateapp.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class PrendaDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val colores: List<String> = emptyList(),
    val ocasiones: List<String> = emptyList(),
    val temporada: String = "",
    val fotoUrl: String = "",
    val vecesUsada: Int = 0,
    val ultimaVezUsada: Timestamp? = null,
    val lugarCompra: String = "",
    val lat: Double? = null,
    val lng: Double? = null
)