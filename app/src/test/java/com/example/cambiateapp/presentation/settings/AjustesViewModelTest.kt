package com.example.cambiateapp.presentation.settings

import com.example.cambiateapp.MainDispatcherRule
import com.example.cambiateapp.TemaRepositoryFake
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.usecase.GuardarModoTemaUseCase
import com.example.cambiateapp.domain.usecase.ObservarModoTemaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = TemaRepositoryFake()

    private fun crearViewModel() = AjustesViewModel(
        observarModoTema = ObservarModoTemaUseCase(repositorio),
        guardarModoTema = GuardarModoTemaUseCase(repositorio),
    )

    /** El estado solo se calcula si alguien lo está mirando: este "observador" hace de pantalla. */
    private fun TestScope.observar(viewModel: AjustesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun `el estado inicial es el tema del sistema y sin aviso`() = runTest {
        val vm = crearViewModel()
        observar(vm)
        advanceUntilIdle()

        assertEquals(ModoTema.SISTEMA, vm.uiState.value.modoTema)
        assertNull(vm.uiState.value.aviso)
    }

    @Test
    fun `elegir un tema lo guarda y el estado lo refleja`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(AjustesUiEvent.TemaElegido(ModoTema.OSCURO))
        advanceUntilIdle()

        assertEquals(ModoTema.OSCURO, vm.uiState.value.modoTema)
    }

    @Test
    fun `si guardar falla muestra un aviso y no cambia el tema`() = runTest {
        repositorio.fallarAlGuardar = true
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(AjustesUiEvent.TemaElegido(ModoTema.CLARO))
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.aviso)
        assertEquals(ModoTema.SISTEMA, vm.uiState.value.modoTema)
    }

    @Test
    fun `avisar que el mensaje se mostro lo limpia`() = runTest {
        repositorio.fallarAlGuardar = true
        val vm = crearViewModel()
        observar(vm)
        vm.onEvent(AjustesUiEvent.TemaElegido(ModoTema.CLARO))
        advanceUntilIdle()

        vm.onEvent(AjustesUiEvent.AvisoMostrado)
        advanceUntilIdle()

        assertNull(vm.uiState.value.aviso)
    }
}