package com.example.cambiateapp.presentation.notifications

import com.example.cambiateapp.AgendadorFake
import com.example.cambiateapp.MainDispatcherRule
import com.example.cambiateapp.NotificadorFake
import com.example.cambiateapp.R
import com.example.cambiateapp.RecordatorioRepositoryFake
import com.example.cambiateapp.TextosFake
import com.example.cambiateapp.UbicacionRepositoryFake
import com.example.cambiateapp.domain.usecase.ConfigurarRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.EnviarNotificacionDePruebaUseCase
import com.example.cambiateapp.domain.usecase.ObservarPreferenciasRecordatorioUseCase
import com.example.cambiateapp.domain.usecase.ObtenerUbicacionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificacionesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = RecordatorioRepositoryFake()
    private val agendador = AgendadorFake()
    private val notificador = NotificadorFake()
    private val ubicacionRepositorio = UbicacionRepositoryFake()
    private val textos = TextosFake()

    private fun crearViewModel() = NotificacionesViewModel(
        observarPreferencias = ObservarPreferenciasRecordatorioUseCase(repositorio),
        configurarRecordatorio = ConfigurarRecordatorioUseCase(repositorio, agendador),
        enviarNotificacionDePrueba = EnviarNotificacionDePruebaUseCase(notificador),
        obtenerUbicacion = ObtenerUbicacionUseCase(ubicacionRepositorio),
        textos = textos,
    )

    /** El estado solo se calcula si alguien lo está mirando: este "observador" hace de pantalla. */
    private fun TestScope.observar(viewModel: NotificacionesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun `el estado inicial usa las preferencias por defecto`() = runTest {
        val vm = crearViewModel()
        observar(vm)
        advanceUntilIdle()

        val estado = vm.uiState.value
        assertFalse(estado.activo)
        assertEquals(20, estado.hora)
        assertEquals(0, estado.minuto)
        assertNull(estado.aviso)
    }

    @Test
    fun `activar guarda, agenda y avisa`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.ActivarCambiado(true))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.activo)
        assertEquals(20 to 0, agendador.programado)
        // Este aviso lleva datos (cuándo suena y la hora actual), por eso se comprueba solo qué texto es.
        assertTrue(vm.uiState.value.aviso!!.startsWith("#${R.string.notif_aviso_activado}:"))
    }

    @Test
    fun `desactivar cancela el aviso`() = runTest {
        val vm = crearViewModel()
        observar(vm)
        vm.onEvent(NotificacionesUiEvent.ActivarCambiado(true))
        advanceUntilIdle()

        vm.onEvent(NotificacionesUiEvent.ActivarCambiado(false))
        advanceUntilIdle()

        assertFalse(vm.uiState.value.activo)
        assertEquals(1, agendador.cancelaciones)
        assertEquals(textos.obtener(R.string.notif_aviso_desactivado), vm.uiState.value.aviso)
    }

    @Test
    fun `elegir una hora reprograma con la hora nueva`() = runTest {
        val vm = crearViewModel()
        observar(vm)
        vm.onEvent(NotificacionesUiEvent.ActivarCambiado(true))
        advanceUntilIdle()

        vm.onEvent(NotificacionesUiEvent.HoraElegida(7, 30))
        advanceUntilIdle()

        assertEquals(7, vm.uiState.value.hora)
        assertEquals(30, vm.uiState.value.minuto)
        assertEquals(7 to 30, agendador.programado)
    }

    @Test
    fun `probar ahora con permiso muestra la notificacion`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.ProbarAhora)
        advanceUntilIdle()

        assertNotNull(notificador.ultimoConsejo)
        assertEquals(textos.obtener(R.string.notif_aviso_probar_ok), vm.uiState.value.aviso)
    }

    @Test
    fun `probar ahora sin permiso avisa que falta el permiso`() = runTest {
        notificador.conPermiso = false
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.ProbarAhora)
        advanceUntilIdle()

        assertNull(notificador.ultimoConsejo)
        assertEquals(textos.obtener(R.string.notif_aviso_sin_permiso_notif), vm.uiState.value.aviso)
    }

    @Test
    fun `si rechazan el permiso se avisa`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.PermisoRechazado)
        advanceUntilIdle()

        assertEquals(textos.obtener(R.string.notif_aviso_permiso_rechazado), vm.uiState.value.aviso)
    }

    @Test
    fun `el aviso se limpia cuando ya se mostro`() = runTest {
        val vm = crearViewModel()
        observar(vm)
        vm.onEvent(NotificacionesUiEvent.PermisoRechazado)
        advanceUntilIdle()

        vm.onEvent(NotificacionesUiEvent.AvisoMostrado)
        advanceUntilIdle()

        assertNull(vm.uiState.value.aviso)
    }

    @Test
    fun `si conceden la ubicacion se consulta y no se avisa nada`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.UbicacionConcedida)
        advanceUntilIdle()

        assertEquals(1, ubicacionRepositorio.consultas)
        assertNull(vm.uiState.value.aviso)
    }

    @Test
    fun `si no se consigue la ubicacion se avisa que revise el GPS`() = runTest {
        ubicacionRepositorio.ubicacion = null
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.UbicacionConcedida)
        advanceUntilIdle()

        assertEquals(textos.obtener(R.string.error_ubicacion_gps), vm.uiState.value.aviso)
    }

    @Test
    fun `si falla la consulta de ubicacion se avisa sin cerrarse`() = runTest {
        ubicacionRepositorio.fallar = true
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.UbicacionConcedida)
        advanceUntilIdle()

        assertEquals(textos.obtener(R.string.error_ubicacion), vm.uiState.value.aviso)
    }

    @Test
    fun `si rechazan la ubicacion se avisa`() = runTest {
        val vm = crearViewModel()
        observar(vm)

        vm.onEvent(NotificacionesUiEvent.UbicacionRechazada)
        advanceUntilIdle()

        assertEquals(textos.obtener(R.string.notif_aviso_ubicacion_rechazada), vm.uiState.value.aviso)
        assertEquals(0, ubicacionRepositorio.consultas)
    }
}