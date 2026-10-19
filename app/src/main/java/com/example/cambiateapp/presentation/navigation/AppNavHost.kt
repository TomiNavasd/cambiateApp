package com.example.cambiateapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.cambiateapp.DeepLinks
import com.example.cambiateapp.presentation.auth.LoginRoute
import com.example.cambiateapp.presentation.auth.RegisterRoute
import com.example.cambiateapp.presentation.map.MapaRoute
import com.example.cambiateapp.presentation.notifications.NotificacionesRoute
import com.example.cambiateapp.presentation.outfits.OutfitsRoute
import com.example.cambiateapp.presentation.wardrobe.AltaEdicionRoute
import com.example.cambiateapp.presentation.wardrobe.PrendaDetailRoute
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
                onOutfitsClick = { navController.navigate(Ruta.Outfits.route) },
                onRecordatoriosClick = { navController.navigate(Ruta.Notificaciones.route) },
                onMapaClick = { navController.navigate(Ruta.Mapa.route) },
                onCerrarSesion = onCerrarSesion
            )
        }

        composable(
            route = Ruta.Detalle.route,
            arguments = listOf(
                navArgument(Ruta.Detalle.ARG_ID) { type = NavType.StringType }
            )
        ) {
            PrendaDetailRoute(
                onEditar = { id -> navController.navigate(Ruta.AltaEdicion.crear(id)) },
                onEliminada = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
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
        ) {
            AltaEdicionRoute(
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Ruta.Outfits.route,
            deepLinks = listOf(navDeepLink { uriPattern = DeepLinks.OUTFITS })
        ) {
            OutfitsRoute(onVolver = { navController.popBackStack() })
        }

        composable(Ruta.Notificaciones.route) {
            NotificacionesRoute(onVolver = { navController.popBackStack() })
        }

        composable(Ruta.Mapa.route) {
            MapaRoute(onVolver = { navController.popBackStack() })
        }
    }
}