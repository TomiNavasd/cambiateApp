package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.error.CampoInvalido
import com.example.cambiateapp.domain.error.DomainException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val repo = AuthRepositorySpy()
    private val useCase = LoginUseCase(repo)

    @Test
    fun `email sin formato valido devuelve EmailInvalido y no llama al repositorio`() = runBlocking {
        val resultado = useCase("hola.com", "123456")

        assertSame(DomainException.EmailInvalido, resultado.exceptionOrNull())
        assertEquals(0, repo.llamadasLogin)
    }

    @Test
    fun `contrasena vacia devuelve Validacion de CONTRASENA`() = runBlocking {
        val resultado = useCase("a@b.com", "")

        val error = resultado.exceptionOrNull()
        assertTrue(error is DomainException.Validacion)
        assertEquals(CampoInvalido.CONTRASENA, (error as DomainException.Validacion).campo)
        assertEquals(0, repo.llamadasLogin)
    }

    @Test
    fun `datos validos llaman al repositorio con el email sin espacios`() = runBlocking {
        val resultado = useCase("  a@b.com  ", "123456")

        assertTrue(resultado.isSuccess)
        assertEquals(1, repo.llamadasLogin)
        assertEquals("a@b.com", repo.ultimoEmail)
    }
}