package com.example.cambiateapp.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RutaTest {

    @Test
    fun `detalle arma la ruta con el id`() {
        assertEquals("detalle/abc", Ruta.Detalle.crear("abc"))
    }

    @Test
    fun `alta sin id es la ruta base`() {
        assertEquals("alta_edicion", Ruta.AltaEdicion.crear())
    }

    @Test
    fun `edicion arma la ruta con el id`() {
        assertEquals("alta_edicion?id=abc", Ruta.AltaEdicion.crear("abc"))
    }
}