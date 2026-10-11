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
    fotoUrl = FOTOS.getValue(id)
)

private fun prendasIniciales(): List<Prenda> = listOf(
    prenda("1", "Remera blanca básica", "Algodón, cuello redondo", Categoria.SUPERIOR, listOf("blanco"), listOf(Ocasion.CASUAL, Ocasion.DEPORTE)),
    prenda("2", "Camisa celeste", "Manga larga, corte clásico", Categoria.SUPERIOR, listOf("celeste"), listOf(Ocasion.TRABAJO, Ocasion.FORMAL)),
    prenda("3", "Remera negra", "Algodón, cuello redondo", Categoria.SUPERIOR, listOf("negro"), listOf(Ocasion.FIESTA, Ocasion.CASUAL)),
    prenda("4", "Jean azul", "Corte recto", Categoria.INFERIOR, listOf("azul"), listOf(Ocasion.CASUAL)),
    prenda("5", "Jean negro", "Corte recto", Categoria.INFERIOR, listOf("negro"), listOf(Ocasion.CASUAL, Ocasion.FIESTA)),
    prenda("6", "Zapatillas blancas", "Para todos los días", Categoria.CALZADO, listOf("blanco"), listOf(Ocasion.CASUAL, Ocasion.DEPORTE)),
    prenda("7", "Zapatos de cuero marrón", "Con cordones", Categoria.CALZADO, listOf("marrón"), listOf(Ocasion.TRABAJO, Ocasion.FORMAL)),
    prenda("8", "Campera de jean", "Con botones metálicos", Categoria.ABRIGO, listOf("azul"), listOf(Ocasion.CASUAL)),
    prenda("9", "Blazer negro", "Corte clásico", Categoria.ABRIGO, listOf("negro"), listOf(Ocasion.FORMAL, Ocasion.TRABAJO)),
    prenda("10", "Reloj plateado", "Malla metálica", Categoria.ACCESORIO, listOf("plateado"), listOf(Ocasion.FORMAL, Ocasion.FIESTA))
)


private const val PARAMS = "?fm=jpg&q=70&w=600&auto=format&fit=crop"
private const val BASE = "https://images.unsplash.com/photo-"

private val FOTOS: Map<String, String> = mapOf(
    "1" to BASE + "1651761179569-4ba2aa054997" + PARAMS,
    "2" to BASE + "1602810316693-3667c854239a" + PARAMS,
    "3" to BASE + "1618354691373-d851c5c3a990" + PARAMS,
    "4" to BASE + "1714143136372-ddaf8b606da7" + PARAMS,
    "5" to BASE + "1718252540511-e958742e4165" + PARAMS,
    "6" to BASE + "1544441892-794166f1e3be" + PARAMS,
    "7" to BASE + "1603191659812-ee978eeeef76" + PARAMS,
    "8" to BASE + "1543076447-215ad9ba6923" + PARAMS,
    "9" to BASE + "1585412459212-8def26f7e84c" + PARAMS,
    "10" to BASE + "1587836374828-4dbafa94cf0e" + PARAMS
)