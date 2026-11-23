package cl.duocuc.pagoflex.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FiltroChips
import cl.duocuc.pagoflex.ui.components.Paginador
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.cliente.MisReportesViewModel

@Composable
fun MisReportesScreen(
    onVolver: () -> Unit,
    onVerCompromiso: (Long) -> Unit,
    viewModel: MisReportesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoTodos = stringResource(R.string.filtro_todos)

    RecargaAutomatica(onRecargar = viewModel::recargar)

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_mis_reportes), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item {
                FiltroChips(
                    opciones = listOf<EstadoReporte?>(null) + EstadoReporte.entries,
                    seleccionada = uiState.filtro,
                    onSeleccionar = viewModel::onFiltroSeleccionado,
                    textoDe = { it?.etiqueta ?: textoTodos }
                )
            }
            when {
                uiState.cargando -> item { PantallaCargando() }
                uiState.error != null && uiState.reportes.contenido.isEmpty() ->
                    item { AvisoError(uiState.error ?: "", viewModel::recargar) }
                uiState.reportes.contenido.isEmpty() -> item { TextoSecundario(stringResource(R.string.sin_mis_reportes)) }
            }
            items(uiState.reportes.contenido, key = { it.id }) { reporte ->
                Card(onClick = { onVerCompromiso(reporte.compromisoId) }, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        overlineContent = { Text("${reporte.numero} · ${Fechas.mostrar(reporte.fechaCreacion)}") },
                        headlineContent = { Text(reporte.empresa) },
                        supportingContent = { Text("${reporte.concepto}\n${reporte.motivo.etiqueta}") },
                        trailingContent = {
                            EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                        }
                    )
                }
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
