package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.repository.OutfitRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutfitUseCasesTest {

    private class RepoEspia : OutfitRepository {
        val guardados = mutableListOf<Outfit>()
        val eliminados = mutableListOf<String>()

        override fun observarOutfits(): Flow<List<Outfit>> = flowOf(emptyList())

        override suspend fun guardarOutfit(outfit: Outfit): Result<Unit> {
            guardados += outfit
            return Result.success(Unit)
        }

        override suspend fun eliminarOutfit(id: String): Result<Unit> {
            eliminados += id
            return Result.success(Unit)
        }
    }

    private fun outfit(prendas: List<String>) = Outfit(
        id = "",
        ocasion = Ocasion.CASUAL,
        prendasIds = prendas,
        fecha = Instant.EPOCH,
        favorito = false
    )

    @Test
    fun `guardar un outfit sin prendas falla con Validacion y no llama al repositorio`() = runBlocking {
        val repo = RepoEspia()

        val resultado = SaveOutfitUseCase(repo)(outfit(emptyList()))

        val error = resultado.exceptionOrNull()
        assertTrue(error is DomainException.Validacion)
        assertEquals(CampoInvalido.PRENDAS, (error as DomainException.Validacion).campo)
        assertTrue(repo.guardados.isEmpty())
    }

    @Test
    fun `guardar un outfit con prendas lo manda al repositorio`() = runBlocking {
        val repo = RepoEspia()

        val resultado = SaveOutfitUseCase(repo)(outfit(listOf("a", "b", "c")))

        assertTrue(resultado.isSuccess)
        assertEquals(1, repo.guardados.size)
    }

    @Test
    fun `eliminar delega el id al repositorio`() = runBlocking {
        val repo = RepoEspia()

        DeleteOutfitUseCase(repo)("o1")

        assertEquals(listOf("o1"), repo.eliminados)
    }
}