package cl.duocuc.pagoflex.ui.screens.cliente

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
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoBusqueda
import cl.duocuc.pagoflex.ui.components.CampoFecha
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.Paginador
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.ui.theme.EstadoVencido
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.cliente.HistorialViewModel

@Composable
fun HistorialScreen(
    onVolver: () -> Unit,
    onVerComprobante: (Long) -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textoTodas = stringResource(R.string.filtro_todas)

    RecargaAutomatica(onRecargar = viewModel::recargar)

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
                CampoSeleccion(
                    etiqueta = stringResource(R.string.campo_empresa),
                    opciones = listOf<String?>(null) + uiState.empresas,
                    seleccionada = uiState.empresa,
                    onSeleccionar = viewModel::onEmpresaSeleccionada,
                    textoDe = { it ?: textoTodas },
                    icono = R.drawable.ic_empresa,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoFecha(
                        etiqueta = stringResource(R.string.campo_desde),
                        fecha = uiState.desde,
                        onFechaElegida = viewModel::onDesdeElegida,
                        error = uiState.errorFechas,
                        permiteBorrar = true,
                        modifier = Modifier.weight(1f)
                    )
                    CampoFecha(
                        etiqueta = stringResource(R.string.campo_hasta),
                        fecha = uiState.hasta,
                        onFechaElegida = viewModel::onHastaElegida,
                        permiteBorrar = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                CampoBusqueda(
                    texto = uiState.buscar,
                    onTextoCambia = viewModel::onBuscarCambia,
                    sugerencia = stringResource(R.string.buscar_pagos)
                )
            }
            when {
                uiState.cargando -> item { PantallaCargando() }
                uiState.error != null && uiState.pagos.contenido.isEmpty() ->
                    item { AvisoError(uiState.error ?: "", viewModel::recargar) }
                uiState.pagos.contenido.isEmpty() -> item { TextoSecundario(stringResource(R.string.historial_vacio)) }
            }
            items(uiState.pagos.contenido, key = { it.id }) { pago ->
                TarjetaPago(pago = pago, onClick = { onVerComprobante(pago.compromisoId) })
            }
            item {
                Paginador(
                    pagina = uiState.pagos,
                    onAnterior = viewModel::onPaginaAnterior,
                    onSiguiente = viewModel::onPaginaSiguiente
                )
            }
        }
    }
}

@Composable
private fun TarjetaPago(pago: Pago, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text("${pago.empresa} · ${pago.numeroComprobante}") },
            headlineContent = { Text(Formato.pesos(pago.montoPagado)) },
            supportingContent = { Text("${pago.concepto}\n${Fechas.mostrar(pago.fechaHora)}, ${pago.canal}") },
            trailingContent = {
                EtiquetaEstado(
                    texto = stringResource(if (pago.aTiempo) R.string.a_tiempo else R.string.con_atraso),
                    color = if (pago.aTiempo) EstadoPagado else EstadoVencido
                )
            }
        )
    }
}
