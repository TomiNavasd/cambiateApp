package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.OutfitSuggester
import com.example.cambiateapp.domain.repository.PrendaRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestOutfitsUseCaseTest {

    private class RepoPrendasFalso(private val flujo: Flow<List<Prenda>>) : PrendaRepository {
        override fun observarPrendas(): Flow<List<Prenda>> = flujo
        override suspend fun obtenerPrenda(id: String): Prenda? = null
        override suspend fun guardarPrenda(prenda: Prenda): Result<Unit> = Result.success(Unit)
        override suspend fun eliminarPrenda(id: String): Result<Unit> = Result.success(Unit)
    }

    private class SugeridorEspia : OutfitSuggester {
        var llamadas = 0
        var ocasionRecibida: Ocasion? = null
        var prendasRecibidas: List<Prenda> = emptyList()

        override suspend fun sugerir(ocasion: Ocasion, prendas: List<Prenda>): Result<List<Outfit>> {
            llamadas++
            ocasionRecibida = ocasion
            prendasRecibidas = prendas
            return Result.success(listOf(Outfit("", ocasion, listOf("a1"), Instant.EPOCH, false)))
        }
    }

    private val remera = Prenda("a1", "Remera", "", Categoria.SUPERIOR, listOf("blanco"), listOf(Ocasion.CASUAL), "")

    @Test
    fun `le pasa al sugeridor la ocasion y las prendas actuales del armario`() = runBlocking {
        val sugeridor = SugeridorEspia()
        val casoDeUso = SuggestOutfitsUseCase(
            ObservePrendasUseCase(RepoPrendasFalso(flowOf(listOf(remera)))),
            sugeridor
        )

        val resultado = casoDeUso(Ocasion.CASUAL)

        assertEquals(1, sugeridor.llamadas)
        assertEquals(Ocasion.CASUAL, sugeridor.ocasionRecibida)
        assertEquals(listOf(remera), sugeridor.prendasRecibidas)
        assertEquals(1, resultado.getOrThrow().size)
    }

    @Test
    fun `si falla la lectura del armario devuelve el error y no llama al sugeridor`() = runBlocking {
        val sugeridor = SugeridorEspia()
        val casoDeUso = SuggestOutfitsUseCase(
            ObservePrendasUseCase(RepoPrendasFalso(flow<List<Prenda>> { throw DomainException.SinConexion })),
            sugeridor
        )

        val resultado = casoDeUso(Ocasion.CASUAL)

        assertEquals(DomainException.SinConexion, resultado.exceptionOrNull())
        assertEquals(0, sugeridor.llamadas)
    }
}