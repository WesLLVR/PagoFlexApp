package cl.duocuc.pagoflex.ui.screens.cuenta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.CampoContrasena
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.Exito
import cl.duocuc.pagoflex.ui.theme.ExitoFondo
import cl.duocuc.pagoflex.viewmodel.cuenta.RecuperarViewModel

@Composable
fun RecuperarScreen(
    onVolver: () -> Unit,
    viewModel: RecuperarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_recuperar), onVolver = onVolver) },
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
            if (uiState.restablecida) {
                MensajeInformativo(
                    texto = stringResource(R.string.contrasena_restablecida),
                    icono = R.drawable.ic_check,
                    colorFondo = ExitoFondo,
                    colorTexto = Exito
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_ir_login),
                    onClick = onVolver,
                    icono = R.drawable.ic_candado,
                    modifier = Modifier.fillMaxWidth()
                )
                return@Column
            }
            TarjetaSeccion(titulo = stringResource(R.string.paso_correo)) {
                TextoSecundario(stringResource(R.string.texto_recuperar))
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = viewModel::onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                if (!uiState.codigoEnviado) {
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_enviar_codigo),
                        onClick = viewModel::onSolicitarCodigo,
                        habilitado = !uiState.cargando,
                        icono = R.drawable.ic_aviso,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    TextButton(onClick = viewModel::onSolicitarCodigo, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(R.string.btn_reenviar_codigo))
                    }
                }
            }

            AnimatedVisibility(visible = uiState.codigoEnviado) {
                TarjetaSeccion(titulo = stringResource(R.string.paso_codigo)) {
                    val codigo = uiState.codigoEnPantalla
                    TextoSecundario(
                        if (codigo == null) stringResource(R.string.texto_codigo_notificacion)
                        else stringResource(R.string.texto_codigo_sin_permiso, codigo)
                    )
                    CampoTexto(
                        valor = uiState.codigo,
                        onValorCambia = viewModel::onCodigoCambia,
                        etiqueta = stringResource(R.string.campo_codigo),
                        icono = R.drawable.ic_numero,
                        error = uiState.errorCodigo,
                        tipoTeclado = KeyboardType.NumberPassword
                    )
                    CampoContrasena(
                        valor = uiState.nueva,
                        onValorCambia = viewModel::onNuevaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena_nueva),
                        error = uiState.errorNueva
                    )
                    CampoContrasena(
                        valor = uiState.repetida,
                        onValorCambia = viewModel::onRepetidaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena_repetir),
                        error = uiState.errorRepetida
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_restablecer),
                        onClick = viewModel::onRestablecer,
                        habilitado = !uiState.cargando,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            AnimatedVisibility(visible = uiState.error != null) {
                MensajeInformativo(
                    texto = uiState.error ?: "",
                    icono = R.drawable.ic_info,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }
        }
    }
}
