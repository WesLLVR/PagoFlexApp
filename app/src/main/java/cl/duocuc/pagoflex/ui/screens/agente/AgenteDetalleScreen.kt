package cl.duocuc.pagoflex.ui.screens.agente

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.FotoRedondeada
import cl.duocuc.pagoflex.ui.components.LineaTiempo
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.agente.AgenteDetalleViewModel

@Composable
fun AgenteDetalleScreen(
    idReporte: Long,
    onVolver: () -> Unit,
    viewModel: AgenteDetalleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    RecargaAutomatica { viewModel.cargar(idReporte) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = uiState.reporte?.let { stringResource(R.string.titulo_reporte_numero, it.numero) }
                    ?: stringResource(R.string.titulo_reportes),
                onVolver = onVolver
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val reporte = uiState.reporte
        if (reporte == null) {
            if (uiState.error != null) {
                AvisoError(uiState.error ?: "", { viewModel.cargar(idReporte) }, Modifier.padding(innerPadding))
            } else {
                PantallaCargando(Modifier.padding(innerPadding))
            }
            return@Scaffold
        }
        val esMio = reporte.agenteId == uiState.idAgente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            if (uiState.procesando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            TarjetaSeccion(titulo = stringResource(R.string.titulo_datos_reporte)) {
                EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                FilaDato(stringResource(R.string.dato_usuario), reporte.nombreUsuario)
                FilaDato(stringResource(R.string.dato_empresa), reporte.empresa)
                FilaDato(stringResource(R.string.dato_folio), reporte.folio)
                FilaDato(stringResource(R.string.dato_concepto), reporte.concepto)
                FilaDato(stringResource(R.string.dato_motivo), reporte.motivo.etiqueta)
                FilaDato(stringResource(R.string.dato_fecha), Fechas.mostrar(reporte.fechaCreacion))
                FilaDato(stringResource(R.string.dato_agente), reporte.nombreAgente ?: "—")
                if (reporte.comentario.isNotBlank()) {
                    HorizontalDivider()
                    Text(text = reporte.comentario, style = MaterialTheme.typography.bodyLarge)
                }
                reporte.fotoUrl?.let {
                    FotoRedondeada(
                        imagen = it,
                        descripcion = stringResource(R.string.cd_foto_comprobante),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            when (reporte.estado) {
                EstadoReporte.ABIERTO -> BotonPrincipal(
                    texto = stringResource(R.string.btn_tomar_reporte),
                    onClick = viewModel::onTomar,
                    habilitado = !uiState.procesando,
                    icono = R.drawable.ic_soporte,
                    modifier = Modifier.fillMaxWidth()
                )
                EstadoReporte.EN_GESTION -> if (esMio) {
                    TarjetaSeccion(titulo = stringResource(R.string.titulo_respuesta)) {
                        CampoTexto(
                            valor = uiState.respuesta,
                            onValorCambia = viewModel::onRespuestaCambia,
                            etiqueta = stringResource(R.string.campo_respuesta),
                            icono = R.drawable.ic_editar,
                            error = uiState.errorRespuesta,
                            ayuda = stringResource(R.string.ayuda_respuesta, uiState.respuesta.length),
                            lineas = 4
                        )
                        BotonPrincipal(
                            texto = stringResource(R.string.btn_resolver),
                            onClick = viewModel::onResolver,
                            habilitado = !uiState.procesando,
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
                } else {
                    TextoSecundario(stringResource(R.string.reporte_de_otro_agente, reporte.nombreAgente ?: ""))
                }
                EstadoReporte.RESUELTO -> TextoSecundario(stringResource(R.string.esperando_usuario))
                EstadoReporte.CERRADO -> TextoSecundario(stringResource(R.string.reporte_cerrado))
            }

            reporte.respuesta?.let {
                TarjetaSeccion(titulo = stringResource(R.string.respuesta_soporte)) {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_seguimiento)) {
                LineaTiempo(cambios = reporte.historial, estadoActual = reporte.estado)
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
