package cl.duocuc.pagoflex.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.notificaciones.AvisosWorker
import cl.duocuc.pagoflex.notificaciones.PedirPermisoNotificaciones

private const val DURACION_TRANSICION = 300

fun rutaInicioDe(rol: Rol?): String = when (rol) {
    Rol.USUARIO_FINAL -> Ruta.Inicio.ruta
    Rol.AGENTE -> Ruta.AgenteReportes.ruta
    Rol.EJECUTIVO -> Ruta.EjecutivoCompromisos.ruta
    null -> Ruta.Login.ruta
}

fun NavHostController.irLimpiando(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.id) { inclusive = true }
    }
}

fun NavBackStackEntry.idArgumento(): Long = arguments?.getString("id")?.toLongOrNull() ?: 0L

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sesion = remember { SesionPreferencias(context) }
    val rutaInicial = remember { if (sesion.haySesion()) rutaInicioDe(sesion.rol()) else Ruta.Login.ruta }

    PedirPermisoNotificaciones()

    val cerrarSesion: () -> Unit = {
        sesion.borrar()
        AvisosWorker.cancelar(context)
        navController.irLimpiando(Ruta.Login.ruta)
    }

    NavHost(
        navController = navController,
        startDestination = rutaInicial,
        enterTransition = { fadeIn(tween(DURACION_TRANSICION)) },
        exitTransition = { fadeOut(tween(DURACION_TRANSICION)) }
    ) {
        navegacionCuenta(navController)
        navegacionCliente(navController, cerrarSesion)
        navegacionAgente(navController, cerrarSesion)
        navegacionEjecutivo(navController, cerrarSesion)
    }
}
