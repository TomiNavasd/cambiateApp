package com.example.cambiateapp.data.repository

import com.example.cambiateapp.domain.model.Categoria
import com.example.cambiateapp.domain.model.Ocasion
import com.example.cambiateapp.domain.model.Prenda
import com.example.cambiateapp.domain.repository.PrendaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePrendaRepository @Inject constructor() : PrendaRepository {

    private val prendas = MutableStateFlow(prendasIniciales())

    override fun observarPrendas(): Flow<List<Prenda>> = prendas.asStateFlow()

    override suspend fun obtenerPrenda(id: String): Prenda? {
        delay(300) // simula la demora de la red
        return prendas.value.firstOrNull { it.id == id }
    }

    override suspend fun guardarPrenda(prenda: Prenda): Result<Unit> {
        delay(500) // simula la demora de la red
        if (prenda.esNueva) {
            val conId = prenda.copy(id = UUID.randomUUID().toString())
            prendas.update { lista -> lista + conId }
        } else {
            prendas.update { lista -> lista.map { if (it.id == prenda.id) prenda else it } }
        }
        return Result.success(Unit)
    }
}

private fun prenda(
    id: String,
    nombre: String,
    descripcion: String,
    categoria: Categoria,
    colores: List<String>,
    ocasiones: List<Ocasion>
) = Prenda(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    categoria = categoria,
    colores = colores,
    ocasiones = ocasiones,
    fotoUrl = "https://picsum.photos/seed/prenda$id/400/500"
)

private fun prendasIniciales(): List<Prenda> = listOf(
    prenda("1", "Remera blanca básica", "Algodón, cuello redondo", Categoria.SUPERIOR, listOf("blanco"), listOf(Ocasion.CASUAL, Ocasion.DEPORTE)),
    prenda("2", "Camisa celeste", "Manga larga, corte clásico", Categoria.SUPERIOR, listOf("celeste"), listOf(Ocasion.TRABAJO, Ocasion.FORMAL)),
    prenda("3", "Remera negra estampada", "Estampa roja en el frente", Categoria.SUPERIOR, listOf("negro", "rojo"), listOf(Ocasion.FIESTA, Ocasion.CASUAL)),
    prenda("4", "Jean azul", "Corte recto", Categoria.INFERIOR, listOf("azul"), listOf(Ocasion.CASUAL)),
    prenda("5", "Pantalón de vestir negro", "Tela liviana", Categoria.INFERIOR, listOf("negro"), listOf(Ocasion.TRABAJO, Ocasion.FORMAL)),
    prenda("6", "Zapatillas blancas", "Para todos los días", Categoria.CALZADO, listOf("blanco"), listOf(Ocasion.CASUAL, Ocasion.DEPORTE)),
    prenda("7", "Zapatos de cuero marrón", "Con cordones", Categoria.CALZADO, listOf("marrón"), listOf(Ocasion.TRABAJO, Ocasion.FORMAL)),
    prenda("8", "Campera de jean", "Con botones metálicos", Categoria.ABRIGO, listOf("azul"), listOf(Ocasion.CASUAL)),
    prenda("9", "Sobretodo gris", "Largo hasta la rodilla", Categoria.ABRIGO, listOf("gris"), listOf(Ocasion.FORMAL, Ocasion.TRABAJO)),
    prenda("10", "Reloj plateado", "Malla metálica", Categoria.ACCESORIO, listOf("plateado"), listOf(Ocasion.FORMAL, Ocasion.FIESTA))
)