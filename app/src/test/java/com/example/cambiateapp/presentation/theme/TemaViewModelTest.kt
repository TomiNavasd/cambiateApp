package com.example.cambiateapp.presentation.theme

import com.example.cambiateapp.MainDispatcherRule
import com.example.cambiateapp.TemaRepositoryFake
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.usecase.ObservarModoTemaUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun TestScope.observar(viewModel: TemaViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.modo.collect { }
        }
    }

    @Test
    fun `sin nada guardado arranca siguiendo al sistema`() = runTest {
        val vm = TemaViewModel(ObservarModoTemaUseCase(TemaRepositoryFake()))
        observar(vm)
        advanceUntilIdle()

        assertEquals(ModoTema.SISTEMA, vm.modo.value)
    }

    @Test
    fun `usa el modo guardado por el usuario`() = runTest {
        val vm = TemaViewModel(ObservarModoTemaUseCase(TemaRepositoryFake(ModoTema.OSCURO)))
        observar(vm)
        advanceUntilIdle()

        assertEquals(ModoTema.OSCURO, vm.modo.value)
    }
}