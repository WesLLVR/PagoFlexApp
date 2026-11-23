package cl.duocuc.pagoflex.ui.screens.cliente

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonesFoto
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.FotoRedondeada
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.cliente.ReporteViewModel

@Composable
fun ReporteScreen(
    idCompromiso: Long,
    onVolver: () -> Unit,
    onEnviado: () -> Unit,
    viewModel: ReporteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idCompromiso) { viewModel.cargar(idCompromiso) }

    LaunchedEffect(uiState.enviado) {
        if (uiState.enviado) onEnviado()
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_reportar), onVolver = onVolver) },
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
            TarjetaSeccion(titulo = compromiso.empresa) {
                FilaDato(stringResource(R.string.dato_folio), compromiso.folio)
                FilaDato(stringResource(R.string.dato_concepto), compromiso.concepto)
                FilaDato(stringResource(R.string.dato_total_pagar), Formato.pesos(compromiso.totalAPagar))
            }
            TarjetaSeccion(titulo = stringResource(R.string.titulo_que_paso)) {
                CampoSeleccion<MotivoReporte?>(
                    etiqueta = stringResource(R.string.campo_motivo),
                    opciones = MotivoReporte.entries,
                    seleccionada = uiState.motivo,
                    onSeleccionar = { motivo -> if (motivo != null) viewModel.onMotivoSeleccionado(motivo) },
                    textoDe = { it?.etiqueta ?: "" },
                    icono = R.drawable.ic_reporte,
                    error = uiState.errorMotivo
                )
                CampoTexto(
                    valor = uiState.comentario,
                    onValorCambia = viewModel::onComentarioCambia,
                    etiqueta = stringResource(R.string.campo_comentario),
                    icono = R.drawable.ic_editar,
                    error = uiState.errorComentario,
                    ayuda = stringResource(R.string.ayuda_comentario, uiState.comentario.length),
                    lineas = 4
                )
            }
            TarjetaSeccion(titulo = stringResource(R.string.titulo_foto)) {
                Text(text = stringResource(R.string.texto_foto), style = MaterialTheme.typography.bodyMedium)
                BotonesFoto(
                    snackbarHostState = snackbarHostState,
                    onFotoTomada = viewModel::onFotoTomada,
                    onImagenElegida = viewModel::onImagenElegida
                )
                uiState.errorFoto?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                AnimatedVisibility(
                    visible = uiState.foto != null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        uiState.foto?.let { bytes ->
                            FotoRedondeada(imagen = bytes, descripcion = stringResource(R.string.cd_foto_comprobante))
                        }
                        TextButton(onClick = viewModel::onQuitarFoto) {
                            Text(stringResource(R.string.btn_quitar_foto))
                        }
                    }
                }
            }
            if (uiState.enviando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            BotonPrincipal(
                texto = stringResource(R.string.btn_enviar_reporte),
                onClick = viewModel::onEnviar,
                habilitado = !uiState.enviando,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
