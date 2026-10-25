package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato

@Composable
fun DetalleCompromisoScreen(
    compromiso: Compromiso,
    reporte: Reporte?,
    motivoNoPagable: String?,
    onVolver: () -> Unit,
    onPagar: () -> Unit,
    onReportar: () -> Unit,
    onVerComprobante: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_detalle), onVolver = onVolver) },
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
                if (compromiso.motivoAnulacion != null) {
                    FilaDato(stringResource(R.string.dato_motivo_anulacion), compromiso.motivoAnulacion)
                }
            }

            if (motivoNoPagable != null && compromiso.estado != EstadoCompromiso.PAGADO) {
                MensajeInformativo(
                    texto = motivoNoPagable,
                    icono = R.drawable.ic_info,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }

            if (compromiso.sePuedePagar) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_pagar_monto, Formato.pesos(compromiso.totalAPagar)),
                    onClick = onPagar,
                    habilitado = motivoNoPagable == null,
                    icono = R.drawable.ic_tarjeta,
                    modifier = Modifier.fillMaxWidth()
                )
                BotonSecundario(
                    texto = stringResource(R.string.btn_reportar),
                    onClick = onReportar,
                    icono = R.drawable.ic_reporte,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (compromiso.estado == EstadoCompromiso.PAGADO) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_ver_comprobante),
                    onClick = onVerComprobante,
                    icono = R.drawable.ic_recibo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (reporte != null) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_tu_reporte, reporte.numero)) {
                    EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                    FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
                    FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fecha))
                    if (reporte.comentario.isNotBlank()) {
                        Text(text = reporte.comentario, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (reporte.respuesta != null) {
                        HorizontalDivider()
                        Text(text = stringResource(R.string.respuesta_soporte), style = MaterialTheme.typography.titleMedium)
                        Text(text = reporte.respuesta, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
