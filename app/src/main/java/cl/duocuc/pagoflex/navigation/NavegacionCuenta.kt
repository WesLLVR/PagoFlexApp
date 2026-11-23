package cl.duocuc.pagoflex.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.duocuc.pagoflex.ui.screens.cuenta.LoginScreen
import cl.duocuc.pagoflex.ui.screens.cuenta.PerfilScreen
import cl.duocuc.pagoflex.ui.screens.cuenta.RecuperarScreen

fun NavGraphBuilder.navegacionCuenta(navController: NavHostController) {
    composable(Ruta.Login.ruta) {
        LoginScreen(
            onIngresado = { usuario -> navController.irLimpiando(rutaInicioDe(usuario.rol)) },
            onRecuperar = { navController.navigate(Ruta.Recuperar.ruta) }
        )
    }
    composable(Ruta.Recuperar.ruta) {
        RecuperarScreen(onVolver = { navController.navigateUp() })
    }
    composable(Ruta.Perfil.ruta) {
        PerfilScreen(onVolver = { navController.navigateUp() })
    }
}
