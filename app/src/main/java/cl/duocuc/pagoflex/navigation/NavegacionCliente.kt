package cl.duocuc.pagoflex.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import cl.duocuc.pagoflex.ui.screens.cliente.AvisosScreen
import cl.duocuc.pagoflex.ui.screens.cliente.ComprobanteScreen
import cl.duocuc.pagoflex.ui.screens.cliente.CumplimientoScreen
import cl.duocuc.pagoflex.ui.screens.cliente.DetalleCompromisoScreen
import cl.duocuc.pagoflex.ui.screens.cliente.HistorialScreen
import cl.duocuc.pagoflex.ui.screens.cliente.InicioScreen
import cl.duocuc.pagoflex.ui.screens.cliente.MisReportesScreen
import cl.duocuc.pagoflex.ui.screens.cliente.PagoScreen
import cl.duocuc.pagoflex.ui.screens.cliente.ReporteScreen

fun NavGraphBuilder.navegacionCliente(navController: NavHostController, onCerrarSesion: () -> Unit) {
    composable(Ruta.Inicio.ruta) {
        InicioScreen(
            onVerCompromiso = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
            onVerHistorial = { navController.navigate(Ruta.Historial.ruta) },
            onVerReportes = { navController.navigate(Ruta.MisReportes.ruta) },
            onVerCumplimiento = { navController.navigate(Ruta.Cumplimiento.ruta) },
            onConfigurarAvisos = { navController.navigate(Ruta.Avisos.ruta) },
            onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
            onCerrarSesion = onCerrarSesion
        )
    }
    composable(Ruta.Detalle.ruta) { entrada ->
        DetalleCompromisoScreen(
            idCompromiso = entrada.idArgumento(),
            onVolver = { navController.navigateUp() },
            onPagar = { id -> navController.navigate(Ruta.Pago.crear(id)) },
            onReportar = { id -> navController.navigate(Ruta.Reporte.crear(id)) },
            onVerComprobante = { id -> navController.navigate(Ruta.Comprobante.crear(id)) },
            onVerCuota = { id ->
                navController.navigate(Ruta.Detalle.crear(id)) {
                    popUpTo(Ruta.Detalle.ruta) { inclusive = true }
                }
            }
        )
    }
    composable(Ruta.Pago.ruta) { entrada ->
        PagoScreen(
            idCompromiso = entrada.idArgumento(),
            onVolver = { navController.navigateUp() },
            onPagado = { id ->
                navController.navigate(Ruta.Comprobante.crear(id)) {
                    popUpTo(Ruta.Inicio.ruta)
                }
            }
        )
    }
    composable(Ruta.Comprobante.ruta) { entrada ->
        ComprobanteScreen(
            idCompromiso = entrada.idArgumento(),
            onVolver = { navController.navigateUp() },
            onIrAlInicio = { navController.popBackStack(Ruta.Inicio.ruta, inclusive = false) },
            onPagarSiguiente = { id ->
                navController.navigate(Ruta.Pago.crear(id)) {
                    popUpTo(Ruta.Inicio.ruta)
                }
            }
        )
    }
    composable(Ruta.Reporte.ruta) { entrada ->
        ReporteScreen(
            idCompromiso = entrada.idArgumento(),
            onVolver = { navController.navigateUp() },
            onEnviado = { navController.navigateUp() }
        )
    }
    composable(Ruta.Historial.ruta) {
        HistorialScreen(
            onVolver = { navController.navigateUp() },
            onVerComprobante = { id -> navController.navigate(Ruta.Comprobante.crear(id)) }
        )
    }
    composable(Ruta.MisReportes.ruta) {
        MisReportesScreen(
            onVolver = { navController.navigateUp() },
            onVerCompromiso = { id -> navController.navigate(Ruta.Detalle.crear(id)) }
        )
    }
    composable(Ruta.Cumplimiento.ruta) {
        CumplimientoScreen(onVolver = { navController.navigateUp() })
    }
    composable(Ruta.Avisos.ruta) {
        AvisosScreen(onVolver = { navController.navigateUp() })
    }
}
