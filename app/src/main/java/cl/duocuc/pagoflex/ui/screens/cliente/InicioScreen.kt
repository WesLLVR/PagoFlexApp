package cl.duocuc.pagoflex.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.FiltroCompromiso
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoBusqueda
import cl.duocuc.pagoflex.ui.components.FiltroChips
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.OpcionMenu
import cl.duocuc.pagoflex.ui.components.Paginador
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaCompromiso
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.cliente.InicioViewModel
import kotlinx.coroutines.launch

private val filtrosCliente = listOf(
    FiltroCompromiso.VIGENTES,
    FiltroCompromiso.VENCIDOS,
    FiltroCompromiso.EN_REVISION,
    FiltroCompromiso.PAGADOS,
    FiltroCompromiso.ANULADOS,
    FiltroCompromiso.TODOS
)

@Composable
fun InicioScreen(
    onVerCompromiso: (Long) -> Unit,
    onVerHistorial: () -> Unit,
    onVerReportes: () -> Unit,
    onVerCumplimiento: () -> Unit,
    onConfigurarAvisos: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: InicioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    RecargaAutomatica(onRecargar = viewModel::recargar)

    MenuLateral(
        drawerState = drawerState,
        nombre = uiState.nombre,
        descripcion = stringResource(R.string.perfil_usuario),
        opciones = listOf(
            OpcionMenu(stringResource(R.string.menu_historial), R.drawable.ic_historial, onVerHistorial),
            OpcionMenu(stringResource(R.string.menu_mis_reportes), R.drawable.ic_reporte, onVerReportes),
            OpcionMenu(stringResource(R.string.menu_cumplimiento), R.drawable.ic_estrella, onVerCumplimiento),
            OpcionMenu(stringResource(R.string.menu_avisos), R.drawable.ic_aviso, onConfigurarAvisos),
            OpcionMenu(stringResource(R.string.menu_perfil), R.drawable.ic_persona, onVerPerfil)
        ),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = stringResource(R.string.saludo, uiState.nombre.substringBefore(" ")),
                    onAbrirMenu = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (uiState.cargando) {
                PantallaCargando(Modifier.padding(innerPadding))
                return@Scaffold
            }
            if (uiState.error != null && uiState.compromisos.totalElementos == 0L && uiState.buscar.isEmpty()) {
                AvisoError(uiState.error ?: "", viewModel::recargar, Modifier.padding(innerPadding))
                return@Scaffold
            }
            val resumen = uiState.resumen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                item {
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_resumen_mes)) {
                        FilaDato(stringResource(R.string.total_por_pagar), Formato.pesos(resumen.totalMes), destacado = true)
                        FilaDato(
                            stringResource(R.string.proximo_vencimiento),
                            resumen.proximoVencimiento?.let { "${Fechas.mostrar(it)}\n${resumen.empresaProximo ?: ""}" } ?: "—"
                        )
                        FilaDato(stringResource(R.string.compromisos_vencidos), resumen.cantidadVencidos.toString())
                    }
                }
                items(uiState.avisos, key = { "aviso-${it.idCompromiso}" }) { aviso ->
                    MensajeInformativo(
                        texto = aviso.mensaje,
                        icono = R.drawable.ic_aviso,
                        colorFondo = AdvertenciaFondo,
                        colorTexto = Advertencia
                    )
                }
                item {
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
                }
                item {
                    Text(text = stringResource(R.string.titulo_mis_compromisos), style = MaterialTheme.typography.titleMedium)
                }
                item {
                    FiltroChips(
                        opciones = filtrosCliente,
                        seleccionada = uiState.filtro,
                        onSeleccionar = viewModel::onFiltroSeleccionado,
                        textoDe = { it.etiqueta }
                    )
                }
                item {
                    CampoBusqueda(
                        texto = uiState.buscar,
                        onTextoCambia = viewModel::onBuscarCambia,
                        sugerencia = stringResource(R.string.buscar_compromisos)
                    )
                }
                if (uiState.compromisos.contenido.isEmpty()) {
                    item {
                        TextoSecundario(
                            stringResource(
                                if (uiState.filtro == FiltroCompromiso.VIGENTES && uiState.buscar.isEmpty()) R.string.sin_compromisos
                                else R.string.sin_resultados
                            )
                        )
                    }
                }
                items(uiState.compromisos.contenido, key = { it.id }) { compromiso ->
                    TarjetaCompromiso(compromiso = compromiso, onClick = { onVerCompromiso(compromiso.id) })
                }
                item {
                    Paginador(
                        pagina = uiState.compromisos,
                        onAnterior = viewModel::onPaginaAnterior,
                        onSiguiente = viewModel::onPaginaSiguiente
                    )
                }
                item { Spacer(Modifier.height(Dimens.espacioGrande)) }
            }
        }
    }
}
