package cl.duocuc.pagoflex.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.ui.screens.IngresoScreen
import cl.duocuc.pagoflex.ui.screens.agente.AgenteDetalleScreen
import cl.duocuc.pagoflex.ui.screens.agente.AgenteReportesScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoCompromisosScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoDetalleScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoRegistrarScreen
import cl.duocuc.pagoflex.ui.screens.ejecutivo.EjecutivoResumenScreen
import cl.duocuc.pagoflex.ui.screens.usuario.AvisosScreen
import cl.duocuc.pagoflex.ui.screens.usuario.ComprobanteScreen
import cl.duocuc.pagoflex.ui.screens.usuario.CumplimientoScreen
import cl.duocuc.pagoflex.ui.screens.usuario.DetalleCompromisoScreen
import cl.duocuc.pagoflex.ui.screens.usuario.HistorialScreen
import cl.duocuc.pagoflex.ui.screens.usuario.InicioScreen
import cl.duocuc.pagoflex.ui.screens.usuario.PagoScreen
import cl.duocuc.pagoflex.ui.screens.usuario.ReporteScreen
import cl.duocuc.pagoflex.viewmodel.AgenteViewModel
import cl.duocuc.pagoflex.viewmodel.EjecutivoViewModel
import cl.duocuc.pagoflex.viewmodel.SesionViewModel
import cl.duocuc.pagoflex.viewmodel.UsuarioFinalViewModel

private const val DURACION_TRANSICION = 300

private fun rutaInicioDe(usuario: Usuario?): String = when (usuario?.rol) {
    Rol.USUARIO_FINAL -> Ruta.Inicio.ruta
    Rol.AGENTE -> Ruta.AgenteReportes.ruta
    Rol.EJECUTIVO -> Ruta.EjecutivoCompromisos.ruta
    null -> Ruta.Ingreso.ruta
}

private fun NavHostController.irLimpiando(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    val sesionViewModel: SesionViewModel = viewModel()
    val usuarioFinalViewModel: UsuarioFinalViewModel = viewModel()
    val agenteViewModel: AgenteViewModel = viewModel()
    val ejecutivoViewModel: EjecutivoViewModel = viewModel()

    val usuarioActual by sesionViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarioFinal by usuarioFinalViewModel.uiState.collectAsStateWithLifecycle()
    val agente by agenteViewModel.uiState.collectAsStateWithLifecycle()
    val ejecutivo by ejecutivoViewModel.uiState.collectAsStateWithLifecycle()

    val rutaInicial = remember { rutaInicioDe(sesionViewModel.usuarioActual.value) }

    val cerrarSesion: () -> Unit = {
        sesionViewModel.cerrarSesion()
        navController.irLimpiando(Ruta.Ingreso.ruta)
    }

    NavHost(
        navController = navController,
        startDestination = rutaInicial,
        enterTransition = { fadeIn(tween(DURACION_TRANSICION)) },
        exitTransition = { fadeOut(tween(DURACION_TRANSICION)) }
    ) {
        composable(Ruta.Ingreso.ruta) {
            IngresoScreen(
                sesiones = sesionViewModel.sesiones,
                onIngresar = { usuario ->
                    sesionViewModel.iniciarSesion(usuario)
                    when (usuario.rol) {
                        Rol.USUARIO_FINAL -> usuarioFinalViewModel.recargar()
                        Rol.AGENTE -> agenteViewModel.recargar()
                        Rol.EJECUTIVO -> ejecutivoViewModel.recargar()
                    }
                    navController.irLimpiando(rutaInicioDe(usuario))
                }
            )
        }

        composable(Ruta.Inicio.ruta) {
            InicioScreen(
                uiState = usuarioFinal,
                onVerCompromiso = { id -> navController.navigate(Ruta.Detalle.crear(id)) },
                onVerHistorial = { navController.navigate(Ruta.Historial.ruta) },
                onVerCumplimiento = { navController.navigate(Ruta.Cumplimiento.ruta) },
                onConfigurarAvisos = { navController.navigate(Ruta.Avisos.ruta) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val compromiso = usuarioFinal.compromisos.firstOrNull { it.id == id }
            if (compromiso != null) {
                val reporte = remember(compromiso) { usuarioFinalViewModel.reporteDe(compromiso.id) }
                DetalleCompromisoScreen(
                    compromiso = compromiso,
                    reporte = reporte,
                    motivoNoPagable = usuarioFinalViewModel.motivoNoPagable(compromiso),
                    onVolver = { navController.navigateUp() },
                    onPagar = { navController.navigate(Ruta.Pago.crear(compromiso.id)) },
                    onReportar = { navController.navigate(Ruta.Reporte.crear(compromiso.id)) },
                    onVerComprobante = { navController.navigate(Ruta.Comprobante.crear(compromiso.id)) }
                )
            }
        }

        composable(
            route = Ruta.Pago.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val compromiso = remember(id) { usuarioFinalViewModel.compromiso(id) }
            if (compromiso != null) {
                PagoScreen(
                    compromiso = compromiso,
                    onVolver = { navController.navigateUp() },
                    onPagoConfirmado = {
                        usuarioFinalViewModel.recargar()
                        navController.navigate(Ruta.Comprobante.crear(compromiso.id)) {
                            popUpTo(Ruta.Inicio.ruta)
                        }
                    }
                )
            }
        }

        composable(
            route = Ruta.Comprobante.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val pago = remember(id) { usuarioFinalViewModel.pagoDe(id) }
            if (pago != null) {
                ComprobanteScreen(
                    pago = pago,
                    onVolver = { navController.navigateUp() },
                    onIrAlInicio = { navController.popBackStack(Ruta.Inicio.ruta, inclusive = false) }
                )
            }
        }

        composable(
            route = Ruta.Reporte.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val compromiso = remember(id) { usuarioFinalViewModel.compromiso(id) }
            if (compromiso != null) {
                ReporteScreen(
                    compromiso = compromiso,
                    onVolver = { navController.navigateUp() },
                    onEnviado = {
                        usuarioFinalViewModel.recargar()
                        agenteViewModel.recargar()
                        navController.navigateUp()
                    }
                )
            }
        }

        composable(Ruta.Historial.ruta) {
            HistorialScreen(
                onVolver = { navController.navigateUp() },
                onVerComprobante = { id -> navController.navigate(Ruta.Comprobante.crear(id)) }
            )
        }

        composable(Ruta.Cumplimiento.ruta) {
            CumplimientoScreen(
                cumplimiento = usuarioFinal.cumplimiento,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.Avisos.ruta) {
            AvisosScreen(
                onVolver = { navController.navigateUp() },
                onGuardado = { usuarioFinalViewModel.recargar() }
            )
        }

        composable(Ruta.AgenteReportes.ruta) {
            AgenteReportesScreen(
                usuario = usuarioActual,
                uiState = agente,
                onFiltroSeleccionado = agenteViewModel::onFiltroSeleccionado,
                onVerReporte = { id ->
                    agenteViewModel.prepararRespuesta()
                    navController.navigate(Ruta.AgenteDetalle.crear(id))
                },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Ruta.AgenteDetalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val reporte = agente.reportes.firstOrNull { it.id == id }
            if (reporte != null) {
                AgenteDetalleScreen(
                    reporte = reporte,
                    respuesta = agente.respuesta,
                    errorRespuesta = agente.errorRespuesta,
                    onRespuestaCambia = agenteViewModel::onRespuestaCambia,
                    onTomar = { agenteViewModel.tomar(reporte) },
                    onResolver = {
                        if (agenteViewModel.resolver(reporte)) {
                            usuarioFinalViewModel.recargar()
                            ejecutivoViewModel.recargar()
                        }
                    },
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(Ruta.EjecutivoCompromisos.ruta) {
            EjecutivoCompromisosScreen(
                usuario = usuarioActual,
                uiState = ejecutivo,
                onFiltroSeleccionado = ejecutivoViewModel::onFiltroSeleccionado,
                onFiltroMesSeleccionado = ejecutivoViewModel::onFiltroMesSeleccionado,
                onVerCompromiso = { id -> navController.navigate(Ruta.EjecutivoDetalle.crear(id)) },
                onRegistrar = { navController.navigate(Ruta.EjecutivoRegistrar.ruta) },
                onVerResumen = { navController.navigate(Ruta.EjecutivoResumen.ruta) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(
            route = Ruta.EjecutivoDetalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            val compromiso = ejecutivo.compromisos.firstOrNull { it.id == id }
            if (compromiso != null) {
                val reporte = remember(compromiso) { ejecutivoViewModel.reporteDe(compromiso.id) }
                EjecutivoDetalleScreen(
                    compromiso = compromiso,
                    reporte = reporte,
                    uiState = ejecutivo,
                    onPrepararAnulacion = ejecutivoViewModel::prepararAnulacion,
                    onMotivoAnulacion = ejecutivoViewModel::onMotivoAnulacion,
                    onComentarioAnulacion = ejecutivoViewModel::onComentarioAnulacion,
                    onAnular = {
                        val anulado = ejecutivoViewModel.anular(compromiso)
                        if (anulado) usuarioFinalViewModel.recargar()
                        anulado
                    },
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(Ruta.EjecutivoRegistrar.ruta) {
            EjecutivoRegistrarScreen(
                nombreEmpresa = ejecutivo.empresa?.nombre ?: "",
                onVolver = { navController.navigateUp() },
                onRegistrado = {
                    ejecutivoViewModel.recargar()
                    usuarioFinalViewModel.recargar()
                }
            )
        }

        composable(Ruta.EjecutivoResumen.ruta) {
            EjecutivoResumenScreen(
                uiState = ejecutivo,
                onMesAnterior = ejecutivoViewModel::onMesAnterior,
                onMesSiguiente = ejecutivoViewModel::onMesSiguiente,
                onVolver = { navController.navigateUp() }
            )
        }
    }
}
