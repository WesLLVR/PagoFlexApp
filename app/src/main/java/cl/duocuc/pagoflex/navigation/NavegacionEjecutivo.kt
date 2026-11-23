package cl.duocuc.pagoflex.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoCompromisosScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoDetalleScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoRegistrarScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoResumenScreen

fun NavGraphBuilder.navegacionEjecutivo(navController: NavHostController, onCerrarSesion: () -> Unit) {
    composable(Ruta.EjecutivoCompromisos.ruta) {
        EjecutivoCompromisosScreen(
            onVerCompromiso = { id -> navController.navigate(Ruta.EjecutivoDetalle.crear(id)) },
            onRegistrar = { navController.navigate(Ruta.EjecutivoRegistrar.ruta) },
            onVerResumen = { navController.navigate(Ruta.EjecutivoResumen.ruta) },
            onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
            onCerrarSesion = onCerrarSesion
        )
    }
    composable(Ruta.EjecutivoDetalle.ruta) { entrada ->
        EjecutivoDetalleScreen(
            idCompromiso = entrada.idArgumento(),
            onVolver = { navController.navigateUp() }
        )
    }
    composable(Ruta.EjecutivoRegistrar.ruta) {
        EjecutivoRegistrarScreen(onVolver = { navController.navigateUp() })
    }
    composable(Ruta.EjecutivoResumen.ruta) {
        EjecutivoResumenScreen(onVolver = { navController.navigateUp() })
    }
}
