package cl.duocuc.pagoflex.ui.screens.agente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoBusqueda
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FiltroChips
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.OpcionMenu
import cl.duocuc.pagoflex.ui.components.Paginador
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.ErrorSuave
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.agente.AgenteReportesViewModel
import kotlinx.coroutines.launch

@Composable
fun AgenteReportesScreen(
    onVerReporte: (Long) -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: AgenteReportesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val textoTodos = stringResource(R.string.filtro_todos)

    RecargaAutomatica(onRecargar = viewModel::recargar)

    MenuLateral(
        drawerState = drawerState,
        nombre = uiState.nombre,
        descripcion = stringResource(R.string.perfil_agente),
        opciones = listOf(OpcionMenu(stringResource(R.string.menu_perfil), R.drawable.ic_persona, onVerPerfil)),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = stringResource(R.string.titulo_reportes),
                    onAbrirMenu = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            val resumen = uiState.resumen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        TarjetaEstadistica(resumen.abiertos.toString(), stringResource(R.string.estado_abiertos), Modifier.weight(1f))
                        TarjetaEstadistica(resumen.enGestion.toString(), stringResource(R.string.estado_en_gestion), Modifier.weight(1f))
                        TarjetaEstadistica(resumen.atrasados.toString(), stringResource(R.string.estado_atrasados), Modifier.weight(1f))
                    }
                }
                item {
                    FiltroChips(
                        opciones = listOf<EstadoReporte?>(null) + EstadoReporte.entries,
                        seleccionada = uiState.filtro,
                        onSeleccionar = viewModel::onFiltroSeleccionado,
                        textoDe = { it?.etiqueta ?: textoTodos }
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        FilterChip(
                            selected = uiState.soloMios,
                            onClick = { viewModel.onSoloMiosCambia(!uiState.soloMios) },
                            label = { Text(stringResource(R.string.filtro_solo_mios)) }
                        )
                        FilterChip(
                            selected = uiState.soloAtrasados,
                            onClick = { viewModel.onSoloAtrasadosCambia(!uiState.soloAtrasados) },
                            label = { Text(stringResource(R.string.filtro_solo_atrasados)) }
                        )
                    }
                }
                item {
                    CampoBusqueda(
                        texto = uiState.buscar,
                        onTextoCambia = viewModel::onBuscarCambia,
                        sugerencia = stringResource(R.string.buscar_reportes)
                    )
                }
                when {
                    uiState.cargando -> item { PantallaCargando() }
                    uiState.error != null && uiState.reportes.contenido.isEmpty() ->
                        item { AvisoError(uiState.error ?: "", viewModel::recargar) }
                    uiState.reportes.contenido.isEmpty() -> item { TextoSecundario(stringResource(R.string.sin_reportes)) }
                }
                items(uiState.reportes.contenido, key = { it.id }) { reporte ->
                    TarjetaReporte(reporte = reporte, onClick = { onVerReporte(reporte.id) })
                }
                item {
                    Paginador(
                        pagina = uiState.reportes,
                        onAnterior = viewModel::onPaginaAnterior,
                        onSiguiente = viewModel::onPaginaSiguiente
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaReporte(reporte: Reporte, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text("${reporte.numero} · ${Fechas.mostrar(reporte.fechaCreacion)}") },
            headlineContent = { Text(reporte.nombreUsuario) },
            supportingContent = {
                Text("${reporte.empresa}\n${reporte.motivo.etiqueta}" + (reporte.nombreAgente?.let { "\n$it" } ?: ""))
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                    if (reporte.atrasado) {
                        EtiquetaEstado(
                            texto = stringResource(R.string.etiqueta_atrasado),
                            color = ErrorSuave,
                            modifier = Modifier.padding(top = Dimens.espacioMini)
                        )
                    }
                }
            }
        )
    }
}
