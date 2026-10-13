package com.example.cambiateapp.data.ai

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Outfit
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.OutfitSuggester
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random


class ReglasOutfitSuggester(private val random: Random) : OutfitSuggester {

    @Inject
    constructor() : this(Random.Default)

    override suspend fun sugerir(
        ocasion: Ocasion,
        prendas: List<Prenda>
    ): Result<List<Outfit>> {
        val porCategoria = prendas
            .filter { ocasion in it.ocasiones }
            .groupBy { it.categoria }

        fun elegir(categoria: Categoria): List<Prenda> =
            porCategoria[categoria].orEmpty().shuffled(random).take(MAX_POR_CATEGORIA)

        val superiores = elegir(Categoria.SUPERIOR)
        val inferiores = elegir(Categoria.INFERIOR)
        val calzados = elegir(Categoria.CALZADO)
        if (superiores.isEmpty() || inferiores.isEmpty() || calzados.isEmpty()) {
            return Result.success(emptyList())
        }

        val abrigos = elegir(Categoria.ABRIGO)
        val accesorios = elegir(Categoria.ACCESORIO)

        val combinaciones = buildList {
            for (superior in superiores) {
                for (inferior in inferiores) {
                    for (calzado in calzados) add(Triple(superior, inferior, calzado))
                }
            }
        }.shuffled(random).take(MAX_OUTFITS)

        val outfits = combinaciones.mapIndexed { indice, (superior, inferior, calzado) ->
            val prendasDelOutfit = buildList {
                add(superior)
                add(inferior)
                add(calzado)
                if (abrigos.isNotEmpty()) add(abrigos[indice % abrigos.size])
                if (accesorios.isNotEmpty()) add(accesorios[indice % accesorios.size])
            }
            Outfit(
                id = "",
                ocasion = ocasion,
                prendasIds = prendasDelOutfit.map { it.id },
                fecha = Instant.now(),
                favorito = false
            )
        }
        return Result.success(outfits)
    }

    private companion object {
        const val MAX_OUTFITS = 3
        const val MAX_POR_CATEGORIA = 3
    }
}