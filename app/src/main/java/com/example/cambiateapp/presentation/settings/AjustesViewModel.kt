package com.example.cambiateapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.domain.usecase.GuardarModoTemaUseCase
import com.example.cambiateapp.domain.usecase.ObservarModoTemaUseCase
import com.example.cambiateapp.presentation.common.ProveedorDeTextos
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AjustesViewModel @Inject constructor(
    observarModoTema: ObservarModoTemaUseCase,
    private val guardarModoTema: GuardarModoTemaUseCase,
    private val textos: ProveedorDeTextos,
) : ViewModel() {

    private val aviso = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AjustesUiState> =
        combine(observarModoTema(), aviso) { modo, aviso ->
            AjustesUiState(modoTema = modo, aviso = aviso)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AjustesUiState())

    fun onEvent(evento: AjustesUiEvent) {
        when (evento) {
            is AjustesUiEvent.TemaElegido -> guardar(evento.modo)
            AjustesUiEvent.AvisoMostrado -> aviso.value = null
        }
    }

    private fun guardar(modo: ModoTema) {
        viewModelScope.launch {
            try {
                guardarModoTema(modo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                aviso.value = textos.obtener(R.string.ajustes_error_tema)
            }
        }
    }
}