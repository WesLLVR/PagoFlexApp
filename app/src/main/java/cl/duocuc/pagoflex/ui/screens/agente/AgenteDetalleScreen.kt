package cl.duocuc.pagoflex.ui.screens.agente

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.FotoRedondeada
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas

@Composable
fun AgenteDetalleScreen(
    reporte: Reporte,
    respuesta: String,
    errorRespuesta: String?,
    onRespuestaCambia: (String) -> Unit,
    onTomar: () -> Unit,
    onResolver: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_reporte_numero, reporte.numero), onVolver = onVolver) },
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
            TarjetaSeccion(titulo = stringResource(R.string.titulo_datos_reporte)) {
                EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                FilaDato(stringResource(R.string.dato_usuario), reporte.nombreUsuario)
                FilaDato(stringResource(R.string.dato_empresa), reporte.empresa)
                FilaDato(stringResource(R.string.dato_folio), reporte.folio)
                FilaDato(stringResource(R.string.dato_concepto), reporte.concepto)
                FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
                FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fecha))
                FilaDato(stringResource(R.string.dato_agente), reporte.nombreAgente ?: "—")
                if (reporte.comentario.isNotBlank()) {
                    HorizontalDivider()
                    Text(text = reporte.comentario, style = MaterialTheme.typography.bodyLarge)
                }
                if (reporte.foto != null) {
                    FotoRedondeada(
                        bytes = reporte.foto,
                        descripcion = stringResource(R.string.cd_foto_comprobante),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            if (reporte.estado == EstadoReporte.ABIERTO) {
                BotonPrincipal(
                    texto = stringResource(R.string.btn_tomar_reporte),
                    onClick = onTomar,
                    icono = R.drawable.ic_soporte,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = reporte.estado == EstadoReporte.EN_GESTION) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_respuesta)) {
                        CampoTexto(
                            valor = respuesta,
                            onValorCambia = onRespuestaCambia,
                            etiqueta = stringResource(R.string.campo_respuesta),
                            icono = R.drawable.ic_editar,
                            error = errorRespuesta,
                            ayuda = stringResource(R.string.ayuda_respuesta),
                            lineas = 4
                        )
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_resolver),
                            onClick = onResolver,
                            icono = R.drawable.ic_check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MensajeInformativo(
                        texto = stringResource(R.string.agente_no_anula),
                        icono = R.drawable.ic_info,
                        colorFondo = MaterialTheme.colorScheme.primaryContainer,
                        colorTexto = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (reporte.estado == EstadoReporte.RESUELTO) {
                TarjetaSeccion(titulo = stringResource(R.string.respuesta_soporte)) {
                    Text(text = reporte.respuesta ?: "", style = MaterialTheme.typography.bodyLarge)
                    reporte.fechaResolucion?.let {
                        FilaDato(stringResource(R.string.dato_fecha_resolucion), Fechas.mostrar(it))
                    }
                }
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
