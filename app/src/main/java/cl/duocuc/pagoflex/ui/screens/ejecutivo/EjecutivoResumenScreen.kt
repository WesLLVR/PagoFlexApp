package cl.duocuc.pagoflex.ui.screens.ejecutivo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.DatoGrafico
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.GraficoBarras
import cl.duocuc.pagoflex.ui.components.ProgresoAnimado
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoAnulado
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.ui.theme.EstadoPendiente
import cl.duocuc.pagoflex.ui.theme.EstadoRevision
import cl.duocuc.pagoflex.ui.theme.EstadoVencido
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.EjecutivoUiState

@Composable
fun EjecutivoResumenScreen(
    uiState: EjecutivoUiState,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onVolver: () -> Unit
) {
    val resumen = uiState.resumen
    val datos = listOf(
        DatoGrafico(stringResource(R.string.estado_pagados), resumen.pagados, EstadoPagado),
        DatoGrafico(stringResource(R.string.estado_pendientes), resumen.pendientes, EstadoPendiente),
        DatoGrafico(stringResource(R.string.estado_vencidos), resumen.vencidos, EstadoVencido),
        DatoGrafico(stringResource(R.string.estado_revision), resumen.enRevision, EstadoRevision),
        DatoGrafico(stringResource(R.string.estado_anulados), resumen.anulados, EstadoAnulado)
    )

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_resumen), onVolver = onVolver) },
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
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMesAnterior) {
                    Icon(painterResource(R.drawable.ic_anterior), contentDescription = stringResource(R.string.cd_mes_anterior))
                }
                Text(
                    text = Formato.mes(uiState.mes),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMesSiguiente) {
                    Icon(painterResource(R.drawable.ic_siguiente), contentDescription = stringResource(R.string.cd_mes_siguiente))
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_tasa_cobranza)) {
                Text(
                    text = resumen.tasa?.let { Formato.porcentaje(it) } ?: "—",
                    style = MaterialTheme.typography.titleLarge
                )
                ProgresoAnimado(progreso = ((resumen.tasa ?: 0.0) / 100.0).toFloat(), color = EstadoPagado)
                Text(
                    text = stringResource(R.string.formula_tasa, resumen.pagados, resumen.considerados),
                    style = MaterialTheme.typography.bodyMedium
                )
                FilaDato(stringResource(R.string.monto_recaudado), Formato.pesos(resumen.montoRecaudado), destacado = true)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                TarjetaEstadistica(resumen.pagados.toString(), stringResource(R.string.estado_pagados), Modifier.weight(1f))
                TarjetaEstadistica(resumen.pendientes.toString(), stringResource(R.string.estado_pendientes), Modifier.weight(1f))
                TarjetaEstadistica(resumen.vencidos.toString(), stringResource(R.string.estado_vencidos), Modifier.weight(1f))
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_grafico_estados)) {
                GraficoBarras(datos = datos)
            }
        }
    }
}
