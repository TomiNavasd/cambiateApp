package com.example.cambiateapp.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrendaTest {

    private fun prenda(id: String) = Prenda(
        id = id,
        nombre = "Remera",
        descripcion = "",
        categoria = Categoria.SUPERIOR,
        colores = listOf("blanco"),
        ocasiones = listOf(Ocasion.CASUAL),
        fotoUrl = "http://ejemplo/foto.jpg"
    )

    @Test
    fun `id vacio es prenda nueva`() = assertTrue(prenda("").esNueva)

    @Test
    fun `con id no es prenda nueva`() = assertFalse(prenda("abc").esNueva)
}