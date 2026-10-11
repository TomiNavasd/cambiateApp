package com.example.cambiateapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cambiateapp.presentation.auth.LoginRoute
import com.example.cambiateapp.presentation.auth.RegisterRoute
import com.example.cambiateapp.presentation.wardrobe.WardrobeListRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    destinoInicial: String,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        modifier = modifier
    ) {

        composable(Ruta.Login.route) {
            LoginRoute(
                onLoginExitoso = {
                    navController.navigate(Ruta.Lista.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Ruta.Register.route) }
            )
        }

        composable(Ruta.Register.route) {
            RegisterRoute(
                onRegistroExitoso = {
                    navController.navigate(Ruta.Lista.route) {
                        popUpTo(Ruta.Login.route) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.Lista.route) {
            WardrobeListRoute(
                onPrendaClick = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
                onNuevaClick = { navController.navigate(Ruta.AltaEdicion.crear()) },
                onCerrarSesion = onCerrarSesion
            )
        }

        composable(
            route = Ruta.Detalle.route,
            arguments = listOf(
                navArgument(Ruta.Detalle.ARG_ID) { type = NavType.StringType }
            )
        ) { entrada ->
            val id = entrada.arguments?.getString(Ruta.Detalle.ARG_ID).orEmpty()
            PantallaPlaceholder(
                titulo = "Detalle de la prenda: $id",
                botones = listOf(
                    BotonPlaceholder("Editar") {
                        navController.navigate(Ruta.AltaEdicion.crear(id))
                    },
                    BotonPlaceholder("Volver") { navController.popBackStack() }
                )
            )
        }

        composable(
            route = Ruta.AltaEdicion.route,
            arguments = listOf(
                navArgument(Ruta.AltaEdicion.ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entrada ->
            val id = entrada.arguments?.getString(Ruta.AltaEdicion.ARG_ID)
            PantallaPlaceholder(
                titulo = if (id == null) "Nueva prenda" else "Editando la prenda: $id",
                botones = listOf(
                    BotonPlaceholder("Guardar (simulado)") { navController.popBackStack() },
                    BotonPlaceholder("Volver") { navController.popBackStack() }
                )
            )
        }
    }
}