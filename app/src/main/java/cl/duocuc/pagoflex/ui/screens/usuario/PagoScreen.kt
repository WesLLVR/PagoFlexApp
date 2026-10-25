package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.GrupoRadio
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.PagoViewModel

@Composable
fun PagoScreen(
    compromiso: Compromiso,
    onVolver: () -> Unit,
    onPagoConfirmado: () -> Unit,
    viewModel: PagoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.pagoConfirmado) {
        if (uiState.pagoConfirmado) onPagoConfirmado()
    }

    if (uiState.mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelar,
            title = { Text(stringResource(R.string.dialogo_pago_titulo)) },
            text = {
                Text(
                    stringResource(
                        R.string.dialogo_pago_mensaje,
                        Formato.pesos(compromiso.totalAPagar),
                        compromiso.empresa,
                        uiState.medio?.etiqueta ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmar(compromiso) }) {
                    Text(stringResource(R.string.btn_confirmar_pago))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelar) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_pago),
                onVolver = if (uiState.procesando) null else onVolver
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = compromiso.empresa) {
                    FilaDato(stringResource(R.string.dato_concepto), compromiso.concepto)
                    FilaDato(stringResource(R.string.dato_cuota), compromiso.cuota)
                    FilaDato(stringResource(R.string.dato_vencimiento), Fechas.mostrar(compromiso.vencimiento))
                    if (compromiso.recargo > 0) {
                        FilaDato(stringResource(R.string.dato_recargo), Formato.pesos(compromiso.recargo))
                    }
                    FilaDato(stringResource(R.string.dato_total_pagar), Formato.pesos(compromiso.totalAPagar), destacado = true)
                }

                TarjetaSeccion(titulo = stringResource(R.string.titulo_medio_pago)) {
                    GrupoRadio(
                        opciones = MedioPago.entries,
                        seleccionada = uiState.medio,
                        onSeleccionar = viewModel::onMedioSeleccionado,
                        textoDe = { it.etiqueta },
                        habilitado = !uiState.procesando
                    )
                    AnimatedVisibility(visible = uiState.errorMedio != null) {
                        Text(
                            text = uiState.errorMedio ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                MensajeInformativo(
                    texto = stringResource(R.string.pago_seguro),
                    icono = R.drawable.ic_escudo,
                    colorFondo = MaterialTheme.colorScheme.primaryContainer,
                    colorTexto = MaterialTheme.colorScheme.primary
                )

                BotonPrincipal(
                    texto = stringResource(R.string.btn_pagar_monto, Formato.pesos(compromiso.totalAPagar)),
                    onClick = viewModel::onSolicitarPago,
                    habilitado = !uiState.procesando,
                    icono = R.drawable.ic_tarjeta,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = uiState.procesando, modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.procesando_pago),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Dimens.espacioMedio)
                        )
                    }
                }
            }
        }
    }
}
