package cl.duocuc.pagoflex.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.FotoRedondeada
import cl.duocuc.pagoflex.ui.components.LineaTiempo
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.ProgresoAnimado
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.cliente.DetalleCompromisoViewModel

@Composable
fun DetalleCompromisoScreen(
    idCompromiso: Long,
    onVolver: () -> Unit,
    onPagar: (Long) -> Unit,
    onReportar: (Long) -> Unit,
    onVerComprobante: (Long) -> Unit,
    onVerCuota: (Long) -> Unit,
    viewModel: DetalleCompromisoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    RecargaAutomatica { viewModel.cargar(idCompromiso) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    if (uiState.mostrarReabrir) {
        AlertDialog(
            onDismissRequest = { viewModel.onMostrarReabrir(false) },
            title = { Text(stringResource(R.string.dialogo_reabrir_titulo)) },
            text = {
                CampoTexto(
                    valor = uiState.comentarioReabrir,
                    onValorCambia = viewModel::onComentarioReabrir,
                    etiqueta = stringResource(R.string.campo_que_falta),
                    icono = R.drawable.ic_editar,
                    error = uiState.errorReabrir,
                    ayuda = stringResource(R.string.ayuda_reabrir, uiState.comentarioReabrir.length),
                    lineas = 3
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onReabrirReporte, enabled = !uiState.procesando) {
                    Text(stringResource(R.string.btn_reabrir))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onMostrarReabrir(false) }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_detalle), onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val compromiso = uiState.compromiso
        if (compromiso == null) {
            if (uiState.error != null) {
                AvisoError(uiState.error ?: "", { viewModel.cargar(idCompromiso) }, Modifier.padding(innerPadding))
            } else {
                PantallaCargando(Modifier.padding(innerPadding))
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            if (uiState.procesando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            TarjetaSeccion(titulo = compromiso.empresa) {
                Text(text = compromiso.concepto, style = MaterialTheme.typography.bodyMedium)
                EtiquetaEstado(texto = compromiso.estado.etiqueta, color = colorDeEstado(compromiso.estado))
                HorizontalDivider()
                FilaDato(stringResource(R.string.dato_folio), compromiso.folio)
                FilaDato(stringResource(R.string.dato_cuota), compromiso.cuota)
                FilaDato(stringResource(R.string.dato_vencimiento), Fechas.mostrar(compromiso.vencimiento))
                FilaDato(stringResource(R.string.dato_monto), Formato.pesos(compromiso.monto))
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato(stringResource(R.string.dato_dias_atraso), compromiso.diasAtraso.toString())
                    FilaDato(stringResource(R.string.dato_recargo), Formato.pesos(compromiso.recargo))
                }
                if (compromiso.sePuedePagar) {
                    HorizontalDivider()
                    FilaDato(stringResource(R.string.dato_total_pagar), Formato.pesos(compromiso.totalAPagar), destacado = true)
                }
                compromiso.motivoAnulacion?.let { FilaDato(stringResource(R.string.dato_motivo_anulacion), it) }
                compromiso.comentarioAnulacion?.let { if (it.isNotBlank()) TextoSecundario(it) }
            }

            uiState.motivoNoPagable?.let { motivo ->
                MensajeInformativo(
                    texto = motivo,
                    icono = R.drawable.ic_info,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }

            if (compromiso.sePuedePagar) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_pagar_monto, Formato.pesos(compromiso.totalAPagar)),
                    onClick = { onPagar(compromiso.id) },
                    habilitado = uiState.motivoNoPagable == null,
                    icono = R.drawable.ic_tarjeta,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.reporte?.estado.let { it == null || it == EstadoReporte.CERRADO }) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_reportar),
                        onClick = { onReportar(compromiso.id) },
                        icono = R.drawable.ic_reporte,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (compromiso.estado == EstadoCompromiso.PAGADO) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_ver_comprobante),
                    onClick = { onVerComprobante(compromiso.id) },
                    icono = R.drawable.ic_recibo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.cuotas.size > 1) {
                TarjetaCuotas(cuotas = uiState.cuotas, actual = compromiso, onVerCuota = onVerCuota)
            }

            uiState.reporte?.let { reporte ->
                TarjetaReporte(
                    reporte = reporte,
                    procesando = uiState.procesando,
                    onCerrar = viewModel::onCerrarReporte,
                    onReabrir = { viewModel.onMostrarReabrir(true) }
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun TarjetaCuotas(cuotas: List<Compromiso>, actual: Compromiso, onVerCuota: (Long) -> Unit) {
    val pagadas = cuotas.count { it.estado == EstadoCompromiso.PAGADO }
    TarjetaSeccion(titulo = stringResource(R.string.titulo_cuotas)) {
        Text(
            text = stringResource(R.string.cuotas_pagadas, pagadas, cuotas.size),
            style = MaterialTheme.typography.bodyMedium
        )
        ProgresoAnimado(progreso = pagadas.toFloat() / cuotas.size, color = EstadoPagado)
        cuotas.forEach { cuota ->
            val esActual = cuota.id == actual.id
            Card(
                onClick = { if (!esActual) onVerCuota(cuota.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (esActual) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(stringResource(R.string.cuota_numero, cuota.cuota)) },
                    supportingContent = {
                        Text("${Fechas.mostrar(cuota.vencimiento)} · ${Formato.pesos(cuota.totalAPagar)}")
                    },
                    trailingContent = {
                        EtiquetaEstado(texto = cuota.estado.etiqueta, color = colorDeEstado(cuota.estado))
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaReporte(
    reporte: Reporte,
    procesando: Boolean,
    onCerrar: () -> Unit,
    onReabrir: () -> Unit
) {
    TarjetaSeccion(titulo = stringResource(R.string.titulo_tu_reporte, reporte.numero)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
            if (reporte.atrasado) {
                TextoSecundario(
                    stringResource(R.string.reporte_atrasado_cliente),
                    Modifier.padding(start = Dimens.espacioChico)
                )
            }
        }
        FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
        FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fechaCreacion))
        reporte.nombreAgente?.let { FilaDato(stringResource(R.string.dato_agente), it) }
        if (reporte.comentario.isNotBlank()) {
            Text(text = reporte.comentario, style = MaterialTheme.typography.bodyMedium)
        }
        reporte.fotoUrl?.let {
            FotoRedondeada(
                imagen = it,
                descripcion = stringResource(R.string.cd_foto_comprobante),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        reporte.respuesta?.let {
            HorizontalDivider()
            Text(text = stringResource(R.string.respuesta_soporte), style = MaterialTheme.typography.titleMedium)
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        HorizontalDivider()
        Text(text = stringResource(R.string.titulo_seguimiento), style = MaterialTheme.typography.titleMedium)
        LineaTiempo(cambios = reporte.historial, estadoActual = reporte.estado)
        if (reporte.estado == EstadoReporte.RESUELTO) {
            TextoSecundario(stringResource(R.string.pregunta_resuelto))
            BotonPrincipal(
                texto = stringResource(R.string.btn_cerrar_reporte),
                onClick = onCerrar,
                habilitado = !procesando,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
            BotonSecundario(
                texto = stringResource(R.string.btn_sigue_pendiente),
                onClick = onReabrir,
                icono = R.drawable.ic_reporte,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
