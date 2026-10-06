package com.example.cambiateapp.domain.usecase

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

internal fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email)