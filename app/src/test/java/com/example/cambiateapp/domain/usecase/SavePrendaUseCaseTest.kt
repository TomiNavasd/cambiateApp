package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavePrendaUseCaseTest {

    private class RepoEspia : PrendaRepository {
        var guardada: Prenda? = null
        override fun observarPrendas(): Flow<List<Prenda>> = flowOf(emptyList())
        override suspend fun obtenerPrenda(id: String): Prenda? = null
        override suspend fun guardarPrenda(prenda: Prenda): Result<Unit> {
            guardada = prenda
            return Result.success(Unit)
        }
    }

    private fun prendaValida() = Prenda(
        id = "",
        nombre = "Remera",
        descripcion = "",
        categoria = Categoria.SUPERIOR,
        colores = listOf("blanco"),
        ocasiones = listOf(Ocasion.CASUAL),
        fotoUrl = "http://ejemplo/foto.jpg"
    )

    @Test
    fun `nombre vacio falla con Validacion NOMBRE`() {
        runBlocking {
            val repo = RepoEspia()
            val resultado = SavePrendaUseCase(repo)(prendaValida().copy(nombre = "   "))
            val error = resultado.exceptionOrNull()
            assertTrue(error is DomainException.Validacion)
            assertEquals(CampoInvalido.NOMBRE, (error as DomainException.Validacion).campo)
            assertNull(repo.guardada)
        }
    }

    @Test
    fun `foto vacia falla con Validacion FOTO`() {
        runBlocking {
            val repo = RepoEspia()
            val resultado = SavePrendaUseCase(repo)(prendaValida().copy(fotoUrl = ""))
            val error = resultado.exceptionOrNull()
            assertTrue(error is DomainException.Validacion)
            assertEquals(CampoInvalido.FOTO, (error as DomainException.Validacion).campo)
            assertNull(repo.guardada)
        }
    }

    @Test
    fun `prenda valida se guarda con el nombre sin espacios`() {
        runBlocking {
            val repo = RepoEspia()
            val resultado = SavePrendaUseCase(repo)(prendaValida().copy(nombre = "  Remera  "))
            assertTrue(resultado.isSuccess)
            assertNotNull(repo.guardada)
            assertEquals("Remera", repo.guardada?.nombre)
        }
    }
}