package cl.duocuc.pagoflex.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.duocuc.pagoflex.ui.screens.agente.AgenteDetalleScreen
import cl.duocuc.pagoflex.ui.screens.agente.AgenteReportesScreen

fun NavGraphBuilder.navegacionAgente(navController: NavHostController, onCerrarSesion: () -> Unit) {
    composable(Ruta.AgenteReportes.ruta) {
        AgenteReportesScreen(
            onVerReporte = { id -> navController.navigate(Ruta.AgenteDetalle.crear(id)) },
            onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
            onCerrarSesion = onCerrarSesion
        )
    }
    composable(Ruta.AgenteDetalle.ruta) { entrada ->
        AgenteDetalleScreen(
            idReporte = entrada.idArgumento(),
            onVolver = { navController.navigateUp() }
        )
    }
}
