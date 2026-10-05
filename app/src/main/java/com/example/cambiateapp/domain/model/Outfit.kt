package com.example.cambiateapp.domain.model

import java.time.Instant

data class Outfit(
    val id: String,
    val ocasion: Ocasion,
    val prendasIds: List<String>,
    val fecha: Instant,
    val favorito: Boolean
)