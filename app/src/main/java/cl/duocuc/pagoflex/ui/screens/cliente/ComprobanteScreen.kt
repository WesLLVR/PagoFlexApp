package cl.duocuc.pagoflex.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.ProgresoAnimado
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.ui.theme.Exito
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.cliente.ComprobanteViewModel

@Composable
fun ComprobanteScreen(
    idCompromiso: Long,
    onVolver: () -> Unit,
    onIrAlInicio: () -> Unit,
    onPagarSiguiente: (Long) -> Unit,
    viewModel: ComprobanteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idCompromiso) { viewModel.cargar(idCompromiso) }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_comprobante), onVolver = onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val pago = uiState.pago
        if (pago == null) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIconoGrande)
            )
            Text(text = stringResource(R.string.pago_confirmado), style = MaterialTheme.typography.titleLarge, color = Exito)
            TarjetaSeccion(titulo = stringResource(R.string.numero_comprobante, pago.numeroComprobante)) {
                FilaDato(stringResource(R.string.dato_fecha_hora), Fechas.mostrar(pago.fechaHora))
                FilaDato(stringResource(R.string.dato_pagado_por), pago.nombreUsuario)
                FilaDato(stringResource(R.string.dato_rut), Formato.rutOculto(pago.rutUsuario))
                FilaDato(stringResource(R.string.dato_empresa), pago.empresa)
                FilaDato(stringResource(R.string.dato_folio), pago.folio)
                FilaDato(stringResource(R.string.dato_concepto), pago.concepto)
                FilaDato(stringResource(R.string.dato_cuota), pago.cuota)
                FilaDato(stringResource(R.string.dato_vencimiento), Fechas.mostrar(pago.vencimiento))
                HorizontalDivider()
                FilaDato(stringResource(R.string.dato_monto_cuota), Formato.pesos(pago.montoCuota))
                FilaDato(stringResource(R.string.dato_recargo), Formato.pesos(pago.recargo))
                FilaDato(stringResource(R.string.dato_total_pagado), Formato.pesos(pago.montoPagado), destacado = true)
                HorizontalDivider()
                FilaDato(stringResource(R.string.dato_medio_pago), pago.medioPago)
                FilaDato(stringResource(R.string.dato_canal), pago.canal)
                FilaDato(
                    stringResource(R.string.dato_a_tiempo),
                    stringResource(if (pago.aTiempo) R.string.si else R.string.no)
                )
            }
            if (uiState.totalCuotas > 1) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_avance_deuda)) {
                    Text(
                        text = stringResource(R.string.cuotas_pagadas, uiState.cuotasPagadas, uiState.totalCuotas),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    ProgresoAnimado(
                        progreso = uiState.cuotasPagadas.toFloat() / uiState.totalCuotas,
                        color = EstadoPagado
                    )
                    val siguiente = uiState.siguienteCuota
                    if (siguiente != null) {
                        FilaDato(stringResource(R.string.siguiente_cuota), siguiente.cuota)
                        FilaDato(stringResource(R.string.dato_vencimiento), Fechas.mostrar(siguiente.vencimiento))
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_pagar_siguiente, Formato.pesos(siguiente.totalAPagar)),
                            onClick = { onPagarSiguiente(siguiente.id) },
                            icono = R.drawable.ic_siguiente,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(text = stringResource(R.string.deuda_al_dia), style = MaterialTheme.typography.bodyMedium, color = Exito)
                    }
                }
            }
            BotonSecundario(
                texto = stringResource(R.string.btn_ir_inicio),
                onClick = onIrAlInicio,
                icono = R.drawable.ic_inicio,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
