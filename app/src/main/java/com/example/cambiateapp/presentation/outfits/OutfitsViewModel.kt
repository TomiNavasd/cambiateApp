package com.example.cambiateapp.presentation.outfits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cambiateapp.domain.error.DomainException
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.usecase.DeleteOutfitUseCase
import com.example.cambiateapp.domain.usecase.ObserveOutfitsUseCase
import com.example.cambiateapp.domain.usecase.ObservePrendasUseCase
import com.example.cambiateapp.domain.usecase.SaveOutfitUseCase
import com.example.cambiateapp.domain.usecase.SuggestOutfitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OutfitsViewModel @Inject constructor(
    observeOutfits: ObserveOutfitsUseCase,
    observePrendas: ObservePrendasUseCase,
    private val saveOutfit: SaveOutfitUseCase,
    private val deleteOutfit: DeleteOutfitUseCase,
    private val suggestOutfits: SuggestOutfitsUseCase
) : ViewModel() {

    private data class Local(
        val ocasion: Ocasion = Ocasion.CASUAL,
        val sugerencias: List<Outfit> = emptyList(),
        val sugiriendo: Boolean = false,
        val aviso: String? = null
    )

    private val local = MutableStateFlow(Local())
    private val reintento = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OutfitsUiState> = reintento
        .flatMapLatest {
            combine(observeOutfits(), observePrendas(), local) { outfits, prendas, estadoLocal ->
                armarEstado(outfits, prendas, estadoLocal)
            }.catch { emit(OutfitsUiState.Error(mensajeDe(it))) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OutfitsUiState.Cargando)

    fun onEvent(event: OutfitsUiEvent) {
        when (event) {
            is OutfitsUiEvent.OcasionSeleccionada ->
                local.update { it.copy(ocasion = event.ocasion, sugerencias = emptyList()) }
            OutfitsUiEvent.Sugerir -> sugerir()
            is OutfitsUiEvent.GuardarSugerencia -> guardarSugerencia(event.indice)
            is OutfitsUiEvent.DescartarSugerencia -> local.update { actual ->
                actual.copy(sugerencias = actual.sugerencias.filterIndexed { i, _ -> i != event.indice })
            }
            is OutfitsUiEvent.AlternarFavorito -> alternarFavorito(event.outfit)
            is OutfitsUiEvent.Eliminar -> eliminar(event.id)
            OutfitsUiEvent.Reintentar -> reintento.update { it + 1 }
            OutfitsUiEvent.AvisoMostrado -> local.update { it.copy(aviso = null) }
        }
    }

    private fun armarEstado(outfits: List<Outfit>, prendas: List<Prenda>, estadoLocal: Local): OutfitsUiState {
        val porId = prendas.associateBy { it.id }
        fun Outfit.conPrendas() = OutfitConPrendas(this, prendasIds.mapNotNull { porId[it] })
        return OutfitsUiState.Contenido(
            ocasion = estadoLocal.ocasion,
            sugerencias = estadoLocal.sugerencias.map { it.conPrendas() },
            guardados = outfits.map { it.conPrendas() },
            sugiriendo = estadoLocal.sugiriendo,
            aviso = estadoLocal.aviso
        )
    }

    private fun sugerir() {
        val ocasion = local.value.ocasion
        viewModelScope.launch {
            local.update { it.copy(sugiriendo = true, aviso = null) }
            suggestOutfits(ocasion).fold(
                onSuccess = { lista ->
                    local.update {
                        it.copy(
                            sugiriendo = false,
                            sugerencias = lista,
                            aviso = if (lista.isEmpty()) AVISO_SIN_SUGERENCIAS else null
                        )
                    }
                },
                onFailure = { e -> local.update { it.copy(sugiriendo = false, aviso = mensajeDe(e)) } }
            )
        }
    }

    private fun guardarSugerencia(indice: Int) {
        val outfit = local.value.sugerencias.getOrNull(indice) ?: return

        // Un outfit con la misma ocasión y las mismas prendas ya guardado no se repite.
        val guardados = (uiState.value as? OutfitsUiState.Contenido)?.guardados.orEmpty()
        val yaExiste = guardados.any {
            it.outfit.ocasion == outfit.ocasion &&
                    it.outfit.prendasIds.toSet() == outfit.prendasIds.toSet()
        }
        if (yaExiste) {
            local.update { actual ->
                actual.copy(
                    sugerencias = actual.sugerencias.filterNot { it === outfit },
                    aviso = "Ese outfit ya está guardado"
                )
            }
            return
        }

        viewModelScope.launch {
            saveOutfit(outfit).fold(
                onSuccess = {
                    local.update { actual ->
                        actual.copy(
                            sugerencias = actual.sugerencias.filterNot { it === outfit },
                            aviso = "Outfit guardado"
                        )
                    }
                },
                onFailure = { e -> local.update { it.copy(aviso = mensajeDe(e)) } }
            )
        }
    }

    private fun alternarFavorito(outfit: Outfit) {
        viewModelScope.launch {
            saveOutfit(outfit.copy(favorito = !outfit.favorito))
                .onFailure { e -> local.update { it.copy(aviso = mensajeDe(e)) } }
        }
    }

    private fun eliminar(id: String) {
        viewModelScope.launch {
            deleteOutfit(id)
                .onFailure { e -> local.update { it.copy(aviso = mensajeDe(e)) } }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        DomainException.SinConexion -> "Sin conexión. Revisá tu internet e intentá de nuevo."
        DomainException.SesionNoIniciada -> "Tu sesión terminó. Volvé a iniciar sesión."
        DomainException.SinPermisos -> "No tenés permisos para esta acción."
        else -> "Ocurrió un error inesperado. Intentá de nuevo."
    }

    private companion object {
        const val AVISO_SIN_SUGERENCIAS =
            "No hay prendas suficientes para esa ocasión: necesitás al menos una superior, " +
                    "una inferior y un calzado marcados para ella."
    }
}