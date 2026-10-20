package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EnviarRecuperacionContrasenaUseCaseTest {

    private val repo = AuthRepositorySpy()
    private val useCase = EnviarRecuperacionContrasenaUseCase(repo)

    @Test
    fun `email sin formato valido devuelve EmailInvalido y no llama al repositorio`() = runBlocking {
        val resultado = useCase("hola.com")

        assertSame(DomainException.EmailInvalido, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasRecuperacion)
    }

    @Test
    fun `email vacio devuelve EmailInvalido y no llama al repositorio`() = runBlocking {
        val resultado = useCase("")

        assertSame(DomainException.EmailInvalido, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasRecuperacion)
    }

    @Test
    fun `email valido llama al repositorio con el email sin espacios`() = runBlocking {
        val resultado = useCase("  a@b.com  ")

        assertTrue(resultado.isSuccess)
        assertEquals(1, repo.llamadasRecuperacion)
        assertEquals("a@b.com", repo.ultimoEmail)
    }

    @Test
    fun `si el repositorio falla el error llega tal cual`() = runBlocking {
        repo.resultadoRecuperacion = Result.failure(DomainException.SinConexion)

        val resultado = useCase("a@b.com")

        assertSame(DomainException.SinConexion, resultado.exceptionOrNull())
    }
}