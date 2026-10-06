package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {

    private val repo = AuthRepositorySpy()
    private val useCase = RegisterUseCase(repo)

    @Test
    fun `email sin formato valido devuelve EmailInvalido y no llama al repositorio`() = runBlocking {
        val resultado = useCase("sin-arroba", "123456")

        assertSame(DomainException.EmailInvalido, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasRegistrar)
    }

    @Test
    fun `contrasena de 5 caracteres devuelve ContrasenaDebil`() = runBlocking {
        val resultado = useCase("a@b.com", "12345")

        assertSame(DomainException.ContrasenaDebil, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasRegistrar)
    }

    @Test
    fun `contrasena de 6 caracteres y email valido llaman al repositorio`() = runBlocking {
        val resultado = useCase("  a@b.com ", "123456")

        assertTrue(resultado.isSuccess)
        assertEquals(1, repo.llamadasRegistrar)
        assertEquals("a@b.com", repo.ultimoEmail)
    }
}