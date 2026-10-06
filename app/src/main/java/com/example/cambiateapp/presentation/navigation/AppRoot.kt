package com.example.cambiateapp.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@Composable
fun AppRoot(
    estado: SessionUiState,
    onEvent: (SessionEvent) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (estado == SessionUiState.Cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppNavigation(
                estado = estado,
                onEvent = onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AppNavigation(
    estado: SessionUiState,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // La pantalla inicial se decide una sola vez, según la sesión al arrancar.
    val destinoInicial = remember {
        if (estado == SessionUiState.ConSesion) Ruta.Lista.route else Ruta.Login.route
    }

    // Si la sesión se cierra mientras se usa la app: al Login y se vacía la pila.
    var estadoAnterior by remember { mutableStateOf(estado) }
    LaunchedEffect(estado) {
        if (estadoAnterior == SessionUiState.ConSesion && estado == SessionUiState.SinSesion) {
            navController.navigate(Ruta.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        estadoAnterior = estado
    }

    AppNavHost(
        navController = navController,
        destinoInicial = destinoInicial,
        onCerrarSesion = { onEvent(SessionEvent.CerrarSesion) },
        modifier = modifier
    )
}