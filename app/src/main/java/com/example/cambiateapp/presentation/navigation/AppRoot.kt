package com.example.cambiateapp.presentation.navigation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppRoot(
    estado: SessionUiState,
    onEvent: (SessionEvent) -> Unit
) {
    if (estado == SessionUiState.Cargando) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        AppNavigation(estado = estado, onEvent = onEvent)
    }
}

private data class DestinoInferior(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

private val destinosInferiores = listOf(
    DestinoInferior(Ruta.Lista.route, "Armario", Icons.Rounded.Checkroom),
    DestinoInferior(Ruta.Outfits.route, "Outfits", Icons.Rounded.AutoAwesome),
    DestinoInferior(Ruta.Mapa.route, "Mapa", Icons.Rounded.Map)
)

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

    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (destinosInferiores.any { it.ruta == rutaActual }) {
                BarraInferior(rutaActual = rutaActual, navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            destinoInicial = destinoInicial,
            onCerrarSesion = { onEvent(SessionEvent.CerrarSesion) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun BarraInferior(
    rutaActual: String?,
    navController: NavHostController
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        destinosInferiores.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = {
                    navController.navigate(destino.ruta) {
                        popUpTo(Ruta.Lista.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = destino.icono, contentDescription = null) },
                label = { Text(destino.titulo, style = MaterialTheme.typography.labelLarge) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}