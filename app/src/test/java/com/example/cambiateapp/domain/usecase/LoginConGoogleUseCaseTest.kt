package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.DomainException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginConGoogleUseCaseTest {

    private val repo = AuthRepositorySpy()
    private val useCase = LoginConGoogleUseCase(repo)

    @Test
    fun `token vacio devuelve CredencialesInvalidas y no llama al repositorio`() = runBlocking {
        val resultado = useCase("   ")

        assertSame(DomainException.CredencialesInvalidas, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasGoogle)
    }

    @Test
    fun `token valido llama al repositorio con ese token`() = runBlocking {
        val resultado = useCase("token-de-prueba")

        assertTrue(resultado.isSuccess)
        assertEquals(1, repo.llamadasGoogle)
        assertEquals("token-de-prueba", repo.ultimoIdToken)
    }
}