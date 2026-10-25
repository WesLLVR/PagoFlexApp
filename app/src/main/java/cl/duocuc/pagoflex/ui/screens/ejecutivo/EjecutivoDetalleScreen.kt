package cl.duocuc.pagoflex.ui.screens.ejecutivo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.EjecutivoUiState
import kotlinx.coroutines.launch

@Composable
fun EjecutivoDetalleScreen(
    compromiso: Compromiso,
    reporte: Reporte?,
    uiState: EjecutivoUiState,
    onPrepararAnulacion: () -> Unit,
    onMotivoAnulacion: (MotivoAnulacion) -> Unit,
    onComentarioAnulacion: (String) -> Unit,
    onAnular: () -> Boolean,
    onVolver: () -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoAnulado = stringResource(R.string.compromiso_anulado)

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_anular_titulo, compromiso.folio)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoSeleccion<MotivoAnulacion?>(
                        etiqueta = stringResource(R.string.campo_motivo),
                        opciones = MotivoAnulacion.entries,
                        seleccionada = uiState.motivoAnulacion,
                        onSeleccionar = { motivo -> if (motivo != null) onMotivoAnulacion(motivo) },
                        textoDe = { it?.etiqueta ?: "" },
                        icono = R.drawable.ic_anular,
                        error = uiState.errorMotivo
                    )
                    CampoTexto(
                        valor = uiState.comentarioAnulacion,
                        onValorCambia = onComentarioAnulacion,
                        etiqueta = stringResource(R.string.campo_comentario),
                        icono = R.drawable.ic_editar,
                        error = uiState.errorComentario,
                        ayuda = stringResource(R.string.ayuda_comentario_anulacion),
                        lineas = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (onAnular()) {
                        mostrarDialogo = false
                        scope.launch { snackbarHostState.showSnackbar(textoAnulado) }
                    }
                }) { Text(stringResource(R.string.btn_anular)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = compromiso.folio, onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            TarjetaSeccion(titulo = stringResource(R.string.titulo_persona)) {
                FilaDato(stringResource(R.string.dato_nombre), compromiso.nombreUsuario)
                FilaDato(stringResource(R.string.dato_rut), compromiso.rutUsuario)
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_compromiso)) {
                EtiquetaEstado(texto = compromiso.estado.etiqueta, color = colorDeEstado(compromiso.estado))
                FilaDato(stringResource(R.string.dato_concepto), compromiso.concepto)
                FilaDato(stringResource(R.string.dato_cuota), compromiso.cuota)
                FilaDato(stringResource(R.string.dato_monto), Formato.pesos(compromiso.monto))
                FilaDato(stringResource(R.string.dato_vencimiento), Fechas.mostrar(compromiso.vencimiento))
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato(stringResource(R.string.dato_dias_atraso), compromiso.diasAtraso.toString())
                    FilaDato(stringResource(R.string.dato_total_pagar), Formato.pesos(compromiso.totalAPagar), destacado = true)
                }
                if (compromiso.motivoAnulacion != null) {
                    FilaDato(stringResource(R.string.dato_motivo_anulacion), compromiso.motivoAnulacion)
                }
            }

            if (reporte != null) {
                TarjetaSeccion(titulo = stringResource(R.string.titulo_reporte_numero, reporte.numero)) {
                    EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                    FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
                    FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fecha))
                    if (reporte.comentario.isNotBlank()) {
                        HorizontalDivider()
                        Text(text = reporte.comentario, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (compromiso.sePuedeAnular) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_anular_compromiso),
                    onClick = {
                        onPrepararAnulacion()
                        mostrarDialogo = true
                    },
                    icono = R.drawable.ic_anular,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
