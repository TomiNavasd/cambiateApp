package com.example.cambiateapp.domain.error

enum class CampoInvalido { NOMBRE, CATEGORIA, FOTO, EMAIL, CONTRASENA }

sealed class DomainException(cause: Throwable? = null) : Exception(cause) {
    object CredencialesInvalidas : DomainException()
    object EmailYaRegistrado : DomainException()
    object ContrasenaDebil : DomainException()
    object EmailInvalido : DomainException()
    object SesionNoIniciada : DomainException()
    object SinConexion : DomainException()
    object SinPermisos : DomainException()
    class Validacion(val campo: CampoInvalido) : DomainException()
    class Desconocido(cause: Throwable? = null) : DomainException(cause)
}