package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.ui.theme.EstadoVencido
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.HistorialViewModel

@Composable
fun HistorialScreen(
    onVolver: () -> Unit,
    onVerComprobante: (Long) -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoTodas = stringResource(R.string.filtro_todas)
    val textoTodos = stringResource(R.string.filtro_todos)

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_historial), onVolver = onVolver) },
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    CampoSeleccion(
                        etiqueta = stringResource(R.string.campo_empresa),
                        opciones = listOf<String?>(null) + uiState.empresas,
                        seleccionada = uiState.empresaSeleccionada,
                        onSeleccionar = viewModel::onEmpresaSeleccionada,
                        textoDe = { it ?: textoTodas },
                        icono = R.drawable.ic_empresa,
                        modifier = Modifier.weight(1.4f)
                    )
                    CampoSeleccion(
                        etiqueta = stringResource(R.string.campo_anio),
                        opciones = listOf<Int?>(null) + uiState.anios,
                        seleccionada = uiState.anioSeleccionado,
                        onSeleccionar = viewModel::onAnioSeleccionado,
                        textoDe = { it?.toString() ?: textoTodos },
                        icono = R.drawable.ic_calendario,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.pagosFiltrados.isEmpty() && uiState.anuladosFiltrados.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.historial_vacio),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            items(uiState.pagosFiltrados, key = { it.id }) { pago ->
                TarjetaPago(pago = pago, onClick = { onVerComprobante(pago.idCompromiso) })
            }

            if (uiState.anuladosFiltrados.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.titulo_anulados),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Dimens.espacioMedio)
                    )
                }
                items(uiState.anuladosFiltrados, key = { "anulado-${it.id}" }) { compromiso ->
                    TarjetaAnulado(compromiso)
                }
            }
        }
    }
}

@Composable
private fun TarjetaPago(pago: Pago, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text(pago.empresa) },
            headlineContent = { Text(Formato.pesos(pago.montoPagado)) },
            supportingContent = { Text("${pago.concepto}\n${Fechas.mostrar(pago.fechaHora)}, ${pago.canal.etiqueta}") },
            trailingContent = {
                EtiquetaEstado(
                    texto = stringResource(if (pago.aTiempo) R.string.a_tiempo else R.string.con_atraso),
                    color = if (pago.aTiempo) EstadoPagado else EstadoVencido
                )
            }
        )
    }
}

@Composable
private fun TarjetaAnulado(compromiso: Compromiso) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text(compromiso.empresa) },
            headlineContent = { Text(compromiso.concepto) },
            supportingContent = { Text(compromiso.motivoAnulacion ?: "") },
            trailingContent = {
                EtiquetaEstado(
                    texto = EstadoCompromiso.ANULADO.etiqueta,
                    color = colorDeEstado(EstadoCompromiso.ANULADO)
                )
            }
        )
    }
}
