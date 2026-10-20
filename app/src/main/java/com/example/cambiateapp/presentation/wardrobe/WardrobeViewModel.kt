package com.example.cambiateapp.presentation.wardrobe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val observePrendas: ObservePrendasUseCase
) : ViewModel() {

    private val filtro = MutableStateFlow<Categoria?>(null)
    private val consulta = MutableStateFlow("")
    private val reintentos = MutableStateFlow(0)

    val uiState: StateFlow<WardrobeUiState> = reintentos
        .flatMapLatest {
            combine(observePrendas(), filtro, consulta) { prendas, categoria, texto ->
                armarEstado(prendas, categoria, texto)
            }
                .onStart { emit(WardrobeUiState.Cargando) }
                .catch { emit(WardrobeUiState.Error(mensajeDe(it))) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WardrobeUiState.Cargando
        )

    fun onEvent(event: WardrobeUiEvent) {
        when (event) {
            is WardrobeUiEvent.FiltroSeleccionado -> filtro.value = event.categoria
            is WardrobeUiEvent.BusquedaCambiada -> consulta.value = event.texto
            WardrobeUiEvent.Reintentar -> reintentos.update { it + 1 }
        }
    }

    private fun armarEstado(
        prendas: List<Prenda>,
        categoria: Categoria?,
        texto: String
    ): WardrobeUiState {
        if (prendas.isEmpty()) return WardrobeUiState.Vacio
        val buscado = normalizar(texto)
        val visibles = prendas
            .filter { categoria == null || it.categoria == categoria }
            .filter { buscado.isBlank() || coincide(it, buscado) }
        return WardrobeUiState.Contenido(prendas = visibles, filtro = categoria, consulta = texto)
    }

    private fun coincide(prenda: Prenda, buscado: String): Boolean {
        val campos = listOf(prenda.nombre, prenda.categoria.etiqueta()) +
                prenda.colores + prenda.etiquetas
        return campos.any { normalizar(it).contains(buscado) }
    }

    /** Minúsculas y sin tildes, para que "Otoño" y "otono" coincidan. */
    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private fun mensajeDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo"
        DomainException.SinPermisos -> "No tenés permiso para ver estas prendas"
        else -> "No pudimos cargar tu armario"
    }
}