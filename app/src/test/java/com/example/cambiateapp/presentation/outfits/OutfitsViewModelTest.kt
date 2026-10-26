package com.example.cambiateapp.presentation.outfits

import com.example.cambiateapp.MainDispatcherRule
import com.example.cambiateapp.R
import com.example.cambiateapp.TextosFake
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.OutfitRepository
import com.example.cambiateapp.domain.repository.OutfitSuggester
import com.example.cambiateapp.domain.repository.PrendaRepository
import com.example.cambiateapp.domain.usecase.DeleteOutfitUseCase
import com.example.cambiateapp.domain.usecase.ObserveOutfitsUseCase
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
import com.example.cambiateapp.domain.usecase.SaveOutfitUseCase
import com.example.cambiateapp.domain.usecase.SuggestOutfitsUseCase
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OutfitsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val textos = TextosFake()

    // ---- Versiones falsas (en memoria) ----

    private class RepoOutfitsFalso(inicial: List<Outfit> = emptyList()) : OutfitRepository {
        val outfits = MutableStateFlow(inicial)
        val guardados = mutableListOf<Outfit>()
        val eliminados = mutableListOf<String>()
        var falloAlGuardar: Throwable? = null

        override fun observarOutfits(): Flow<List<Outfit>> = outfits

        override suspend fun guardarOutfit(outfit: Outfit): Result<Unit> {
            falloAlGuardar?.let { return Result.failure(it) }
            guardados += outfit
            outfits.update { actuales ->
                if (outfit.esNuevo) actuales + outfit.copy(id = "nuevo${actuales.size}")
                else actuales.map { if (it.id == outfit.id) outfit else it }
            }
            return Result.success(Unit)
        }

        override suspend fun eliminarOutfit(id: String): Result<Unit> {
            eliminados += id
            outfits.update { actuales -> actuales.filterNot { it.id == id } }
            return Result.success(Unit)
        }
    }

    private class RepoPrendasFalso(private val flujo: Flow<List<Prenda>>) : PrendaRepository {
        override fun observarPrendas(): Flow<List<Prenda>> = flujo
        override suspend fun obtenerPrenda(id: String): Prenda? = null
        override suspend fun guardarPrenda(prenda: Prenda): Result<Unit> = Result.success(Unit)
        override suspend fun eliminarPrenda(id: String): Result<Unit> = Result.success(Unit)
    }

    private class SugeridorFalso(var resultado: Result<List<Outfit>>) : OutfitSuggester {
        override suspend fun sugerir(ocasion: Ocasion, prendas: List<Prenda>): Result<List<Outfit>> = resultado
    }

    // ---- Datos de ejemplo ----

    private fun prenda(id: String, categoria: Categoria) =
        Prenda(id, "Prenda $id", "", categoria, listOf("azul"), listOf(Ocasion.CASUAL), "")

    private val armario = listOf(
        prenda("a1", Categoria.SUPERIOR),
        prenda("b1", Categoria.INFERIOR),
        prenda("c1", Categoria.CALZADO)
    )

    private fun outfit(id: String, vararg prendasIds: String, favorito: Boolean = false) =
        Outfit(id, Ocasion.CASUAL, prendasIds.toList(), Instant.EPOCH, favorito)

    private fun crearVm(
        repoOutfits: RepoOutfitsFalso = RepoOutfitsFalso(),
        prendas: Flow<List<Prenda>> = MutableStateFlow(armario),
        sugeridor: SugeridorFalso = SugeridorFalso(Result.success(emptyList()))
    ): OutfitsViewModel {
        val repoPrendas = RepoPrendasFalso(prendas)
        val observarPrendas = ObservePrendasUseCase(repoPrendas)
        return OutfitsViewModel(
            observeOutfits = ObserveOutfitsUseCase(repoOutfits),
            observePrendas = observarPrendas,
            saveOutfit = SaveOutfitUseCase(repoOutfits),
            deleteOutfit = DeleteOutfitUseCase(repoOutfits),
            suggestOutfits = SuggestOutfitsUseCase(observarPrendas, sugeridor),
            textos = textos
        )
    }

    private fun TestScope.observar(vm: OutfitsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    private fun OutfitsViewModel.contenido() = uiState.value as OutfitsUiState.Contenido

    // ---- Tests ----

    @Test
    fun `muestra los outfits guardados con sus prendas y omite las que ya no existen`() = runTest {
        val repo = RepoOutfitsFalso(listOf(outfit("o1", "a1", "b1", "fantasma")))
        val vm = crearVm(repoOutfits = repo)
        observar(vm)

        val guardado = vm.contenido().guardados.single()

        assertEquals(listOf("a1", "b1"), guardado.prendas.map { it.id })
    }

    @Test
    fun `sugerir muestra las sugerencias con sus prendas`() = runTest {
        val vm = crearVm(sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1")))))
        observar(vm)

        vm.onEvent(OutfitsUiEvent.Sugerir)

        val estado = vm.contenido()
        assertEquals(1, estado.sugerencias.size)
        assertEquals(3, estado.sugerencias[0].prendas.size)
        assertEquals(false, estado.sugiriendo)
        assertNull(estado.aviso)
    }

    @Test
    fun `sugerir sin prendas suficientes muestra un aviso`() = runTest {
        val vm = crearVm(sugeridor = SugeridorFalso(Result.success(emptyList())))
        observar(vm)

        vm.onEvent(OutfitsUiEvent.Sugerir)

        val estado = vm.contenido()
        assertTrue(estado.sugerencias.isEmpty())
        assertEquals(textos.obtener(R.string.outfits_aviso_sin_sugerencias), estado.aviso)
    }

    @Test
    fun `sugerir con error muestra un aviso con el motivo`() = runTest {
        val vm = crearVm(sugeridor = SugeridorFalso(Result.failure(DomainException.SinConexion)))
        observar(vm)

        vm.onEvent(OutfitsUiEvent.Sugerir)

        val estado = vm.contenido()
        assertEquals(textos.obtener(R.string.error_sin_conexion), estado.aviso)
        assertEquals(false, estado.sugiriendo)
    }

    @Test
    fun `cambiar de ocasion borra las sugerencias`() = runTest {
        val vm = crearVm(sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1")))))
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.OcasionSeleccionada(Ocasion.TRABAJO))

        val estado = vm.contenido()
        assertEquals(Ocasion.TRABAJO, estado.ocasion)
        assertTrue(estado.sugerencias.isEmpty())
    }

    @Test
    fun `guardar una sugerencia la manda al repositorio y la saca de sugerencias`() = runTest {
        val repo = RepoOutfitsFalso()
        val vm = crearVm(
            repoOutfits = repo,
            sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1"))))
        )
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.GuardarSugerencia(0))

        val estado = vm.contenido()
        assertEquals(1, repo.guardados.size)
        assertTrue(estado.sugerencias.isEmpty())
        assertEquals(1, estado.guardados.size)
        assertEquals(textos.obtener(R.string.outfits_aviso_guardado), estado.aviso)
    }

    @Test
    fun `no guarda un outfit que ya esta guardado aunque las prendas vengan en otro orden`() = runTest {
        val repo = RepoOutfitsFalso(listOf(outfit("o1", "c1", "a1", "b1")))
        val vm = crearVm(
            repoOutfits = repo,
            sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1"))))
        )
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.GuardarSugerencia(0))

        val estado = vm.contenido()
        assertTrue(repo.guardados.isEmpty())
        assertTrue(estado.sugerencias.isEmpty())
        assertEquals(textos.obtener(R.string.outfits_aviso_ya_guardado), estado.aviso)
    }

    @Test
    fun `descartar saca la sugerencia sin guardar nada`() = runTest {
        val repo = RepoOutfitsFalso()
        val vm = crearVm(
            repoOutfits = repo,
            sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1"))))
        )
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.DescartarSugerencia(0))

        assertTrue(vm.contenido().sugerencias.isEmpty())
        assertTrue(repo.guardados.isEmpty())
    }

    @Test
    fun `alternar favorito guarda el outfit con el favorito invertido`() = runTest {
        val original = outfit("o1", "a1", "b1", "c1", favorito = false)
        val repo = RepoOutfitsFalso(listOf(original))
        val vm = crearVm(repoOutfits = repo)
        observar(vm)

        vm.onEvent(OutfitsUiEvent.AlternarFavorito(original))

        assertEquals("o1", repo.guardados.single().id)
        assertEquals(true, repo.guardados.single().favorito)
        assertEquals(true, vm.contenido().guardados.single().outfit.favorito)
    }

    @Test
    fun `eliminar delega el id al repositorio`() = runTest {
        val repo = RepoOutfitsFalso(listOf(outfit("o1", "a1", "b1", "c1")))
        val vm = crearVm(repoOutfits = repo)
        observar(vm)

        vm.onEvent(OutfitsUiEvent.Eliminar("o1"))

        assertEquals(listOf("o1"), repo.eliminados)
        assertTrue(vm.contenido().guardados.isEmpty())
    }

    @Test
    fun `AvisoMostrado limpia el aviso`() = runTest {
        val vm = crearVm(sugeridor = SugeridorFalso(Result.success(emptyList())))
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.AvisoMostrado)

        assertNull(vm.contenido().aviso)
    }

    @Test
    fun `si falla el guardado se muestra un aviso y la sugerencia se conserva`() = runTest {
        val repo = RepoOutfitsFalso().apply { falloAlGuardar = DomainException.SinConexion }
        val vm = crearVm(
            repoOutfits = repo,
            sugeridor = SugeridorFalso(Result.success(listOf(outfit("", "a1", "b1", "c1"))))
        )
        observar(vm)
        vm.onEvent(OutfitsUiEvent.Sugerir)

        vm.onEvent(OutfitsUiEvent.GuardarSugerencia(0))

        val estado = vm.contenido()
        assertEquals(textos.obtener(R.string.error_sin_conexion), estado.aviso)
        assertEquals(1, estado.sugerencias.size)
    }

    @Test
    fun `si falla la carga de prendas el estado es Error`() = runTest {
        val vm = crearVm(prendas = flow<List<Prenda>> { throw DomainException.SinConexion })
        observar(vm)

        val estado = vm.uiState.value

        assertTrue(estado is OutfitsUiState.Error)
        assertEquals(
            textos.obtener(R.string.error_sin_conexion),
            (estado as OutfitsUiState.Error).mensaje
        )
    }
}