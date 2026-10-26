package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.OpcionMenu
import cl.duocuc.pagoflex.ui.components.TarjetaCompromiso
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.UsuarioFinalUiState
import kotlinx.coroutines.launch

@Composable
fun InicioScreen(
    uiState: UsuarioFinalUiState,
    onVerCompromiso: (Long) -> Unit,
    onVerHistorial: () -> Unit,
    onVerCumplimiento: () -> Unit,
    onConfigurarAvisos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val usuario = uiState.usuario
    val resumen = uiState.resumen

    MenuLateral(
        drawerState = drawerState,
        nombre = usuario?.nombre ?: "",
        descripcion = usuario?.rol?.etiqueta ?: "",
        opciones = listOf(
            OpcionMenu(stringResource(R.string.menu_historial), R.drawable.ic_historial, onVerHistorial),
            OpcionMenu(stringResource(R.string.menu_cumplimiento), R.drawable.ic_estrella, onVerCumplimiento),
            OpcionMenu(stringResource(R.string.menu_avisos), R.drawable.ic_aviso, onConfigurarAvisos)
        ),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = stringResource(R.string.saludo, usuario?.primerNombre ?: ""),
                    onAbrirMenu = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_resumen_mes)) {
                    FilaDato(stringResource(R.string.total_por_pagar), Formato.pesos(resumen.totalMes), destacado = true)
                    FilaDato(
                        stringResource(R.string.proximo_vencimiento),
                        resumen.proximoVencimiento?.let { "${Fechas.mostrar(it)}\n${resumen.empresaProximo ?: ""}" } ?: "—"
                    )
                    FilaDato(stringResource(R.string.compromisos_vencidos), resumen.cantidadVencidos.toString())
                }

                uiState.avisos.forEach { aviso ->
                    MensajeInformativo(
                        texto = aviso.mensaje,
                        icono = R.drawable.ic_aviso,
                        colorFondo = AdvertenciaFondo,
                        colorTexto = Advertencia
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaEstadistica(
                        valor = uiState.cumplimiento.porcentaje?.let { Formato.porcentaje(it) } ?: "—",
                        etiqueta = stringResource(R.string.titulo_cumplimiento),
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        valor = uiState.cumplimiento.categoria.etiqueta,
                        etiqueta = stringResource(R.string.categoria),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(text = stringResource(R.string.titulo_mis_compromisos), style = MaterialTheme.typography.titleMedium)
                if (uiState.vigentes.isEmpty()) {
                    Text(text = stringResource(R.string.sin_compromisos), style = MaterialTheme.typography.bodyMedium)
                }
                uiState.vigentes.forEach { compromiso ->
                    TarjetaCompromiso(compromiso = compromiso, onClick = { onVerCompromiso(compromiso.id) })
                }

                Text(
                    text = stringResource(R.string.ultima_actualizacion, uiState.ultimaActualizacion ?: "—"),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}
