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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.ejecutivo.EjecutivoDetalleViewModel

@Composable
fun EjecutivoDetalleScreen(
    idCompromiso: Long,
    onVolver: () -> Unit,
    viewModel: EjecutivoDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmarEliminar by remember { mutableStateOf(false) }

    RecargaAutomatica { viewModel.cargar(idCompromiso) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    LaunchedEffect(uiState.eliminado) {
        if (uiState.eliminado) onVolver()
    }

    val compromiso = uiState.compromiso

    if (uiState.mostrarAnular && compromiso != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onMostrarAnular(false) },
            title = { Text(stringResource(R.string.dialogo_anular_titulo, compromiso.folio)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoSeleccion<MotivoAnulacion?>(
                        etiqueta = stringResource(R.string.campo_motivo),
                        opciones = MotivoAnulacion.entries,
                        seleccionada = uiState.motivoAnulacion,
                        onSeleccionar = { motivo -> if (motivo != null) viewModel.onMotivoAnulacion(motivo) },
                        textoDe = { it?.etiqueta ?: "" },
                        icono = R.drawable.ic_anular,
                        error = uiState.errorMotivo
                    )
                    CampoTexto(
                        valor = uiState.comentarioAnulacion,
                        onValorCambia = viewModel::onComentarioAnulacion,
                        etiqueta = stringResource(R.string.campo_comentario),
                        icono = R.drawable.ic_editar,
                        error = uiState.errorComentario,
                        ayuda = stringResource(R.string.ayuda_comentario_anulacion),
                        lineas = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onAnular, enabled = !uiState.procesando) {
                    Text(stringResource(R.string.btn_anular))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onMostrarAnular(false) }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    if (confirmarEliminar && compromiso != null) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text(stringResource(R.string.dialogo_eliminar_titulo, compromiso.folio)) },
            text = { Text(stringResource(R.string.dialogo_eliminar_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.onEliminar()
                }) { Text(stringResource(R.string.btn_eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = compromiso?.folio ?: "", onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
                compromiso.motivoAnulacion?.let { FilaDato(stringResource(R.string.dato_motivo_anulacion), it) }
                compromiso.comentarioAnulacion?.let { if (it.isNotBlank()) TextoSecundario(it) }
            }
            uiState.reporte?.let { reporte ->
                TarjetaSeccion(titulo = stringResource(R.string.titulo_reporte_numero, reporte.numero)) {
                    EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                    FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
                    FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fechaCreacion))
                    reporte.nombreAgente?.let { FilaDato(stringResource(R.string.dato_agente), it) }
                    if (reporte.comentario.isNotBlank()) {
                        HorizontalDivider()
                        Text(text = reporte.comentario, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (compromiso.sePuedeAnular) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_anular_compromiso),
                    onClick = { viewModel.onMostrarAnular(true) },
                    icono = R.drawable.ic_anular,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (compromiso.estado == EstadoCompromiso.PENDIENTE) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_eliminar_compromiso),
                    onClick = { confirmarEliminar = true },
                    icono = R.drawable.ic_eliminar,
                    modifier = Modifier.fillMaxWidth()
                )
                TextoSecundario(stringResource(R.string.ayuda_eliminar))
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
