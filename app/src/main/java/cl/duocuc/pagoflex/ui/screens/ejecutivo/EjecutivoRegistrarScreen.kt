package cl.duocuc.pagoflex.ui.screens.ejecutivo

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.Exito
import cl.duocuc.pagoflex.ui.theme.ExitoFondo
import cl.duocuc.pagoflex.viewmodel.RegistroCompromisoViewModel

@Composable
fun EjecutivoRegistrarScreen(
    nombreEmpresa: String,
    onVolver: () -> Unit,
    onRegistrado: () -> Unit,
    viewModel: RegistroCompromisoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_registrar), onVolver = onVolver) },
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
            Text(
                text = stringResource(R.string.registrar_para_empresa, nombreEmpresa),
                style = MaterialTheme.typography.bodyLarge
            )

            TarjetaSeccion(titulo = stringResource(R.string.titulo_persona)) {
                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = viewModel::onRutCambia,
                    etiqueta = stringResource(R.string.campo_rut),
                    icono = R.drawable.ic_rut,
                    error = uiState.errorRut,
                    ayuda = uiState.nombrePersona?.let { stringResource(R.string.persona_encontrada, it) }
                        ?: stringResource(if (uiState.personaNueva) R.string.persona_nueva else R.string.ayuda_rut)
                )
                AnimatedVisibility(visible = uiState.personaNueva) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        CampoTexto(
                            valor = uiState.nombre,
                            onValorCambia = viewModel::onNombreCambia,
                            etiqueta = stringResource(R.string.campo_nombre),
                            icono = R.drawable.ic_persona,
                            error = uiState.errorNombre
                        )
                        CampoTexto(
                            valor = uiState.correo,
                            onValorCambia = viewModel::onCorreoCambia,
                            etiqueta = stringResource(R.string.campo_correo),
                            icono = R.drawable.ic_correo,
                            error = uiState.errorCorreo,
                            tipoTeclado = KeyboardType.Email
                        )
                        CampoTexto(
                            valor = uiState.telefono,
                            onValorCambia = viewModel::onTelefonoCambia,
                            etiqueta = stringResource(R.string.campo_telefono),
                            icono = R.drawable.ic_telefono,
                            error = uiState.errorTelefono,
                            tipoTeclado = KeyboardType.Phone
                        )
                    }
                }
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_compromiso)) {
                CampoTexto(
                    valor = uiState.concepto,
                    onValorCambia = viewModel::onConceptoCambia,
                    etiqueta = stringResource(R.string.campo_concepto),
                    icono = R.drawable.ic_editar,
                    error = uiState.errorConcepto
                )
                CampoTexto(
                    valor = uiState.monto,
                    onValorCambia = viewModel::onMontoCambia,
                    etiqueta = stringResource(R.string.campo_monto),
                    icono = R.drawable.ic_dinero,
                    error = uiState.errorMonto,
                    ayuda = stringResource(R.string.ayuda_monto),
                    tipoTeclado = KeyboardType.Number
                )
                CampoTexto(
                    valor = uiState.vencimiento,
                    onValorCambia = viewModel::onVencimientoCambia,
                    etiqueta = stringResource(R.string.campo_vencimiento),
                    icono = R.drawable.ic_calendario,
                    error = uiState.errorVencimiento,
                    ayuda = stringResource(R.string.ayuda_fecha)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoTexto(
                        valor = uiState.cuota,
                        onValorCambia = viewModel::onCuotaCambia,
                        etiqueta = stringResource(R.string.campo_cuota),
                        icono = R.drawable.ic_numero,
                        error = uiState.errorCuota,
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        valor = uiState.totalCuotas,
                        onValorCambia = viewModel::onTotalCuotasCambia,
                        etiqueta = stringResource(R.string.campo_total_cuotas),
                        icono = R.drawable.ic_numero,
                        error = uiState.errorTotalCuotas,
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = stringResource(R.string.ayuda_cuotas),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = uiState.errorGeneral != null) {
                MensajeInformativo(
                    texto = uiState.errorGeneral ?: "",
                    icono = R.drawable.ic_info,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_registrar),
                onClick = { if (viewModel.onRegistrar()) onRegistrado() },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = uiState.registrado) {
                MensajeInformativo(
                    texto = stringResource(R.string.compromiso_registrado),
                    icono = R.drawable.ic_check,
                    colorFondo = ExitoFondo,
                    colorTexto = Exito
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
