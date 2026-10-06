package com.example.cambiateapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cambiateapp.presentation.auth.LoginRoute

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
            PantallaPlaceholder(
                titulo = "Registro (pantalla pendiente)",
                botones = listOf(
                    BotonPlaceholder("Registrarme (simulado)") {
                        navController.navigate(Ruta.Lista.route) {
                            popUpTo(Ruta.Login.route) { inclusive = true }
                        }
                    },
                    BotonPlaceholder("Volver") { navController.popBackStack() }
                )
            )
        }

        composable(Ruta.Lista.route) {
            PantallaPlaceholder(
                titulo = "Lista de prendas (pantalla pendiente)",
                botones = listOf(
                    BotonPlaceholder("Ver detalle de la prenda 1") {
                        navController.navigate(Ruta.Detalle.crear("1"))
                    },
                    BotonPlaceholder("Nueva prenda") {
                        navController.navigate(Ruta.AltaEdicion.crear())
                    },
                    BotonPlaceholder("Cerrar sesión", onCerrarSesion)
                )
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