package cl.duocuc.pagoflex.ui.screens.cuenta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.CampoContrasena
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.viewmodel.cuenta.LoginViewModel

private val cuentasDePrueba = listOf(
    "camila@pagoflex.cl" to R.string.prueba_cliente,
    "andres@pagoflex.cl" to R.string.prueba_agente,
    "paula@pagoflex.cl" to R.string.prueba_ejecutivo
)

@Composable
fun LoginScreen(
    onIngresado: (Usuario) -> Unit,
    onRecuperar: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.ingresado) {
        uiState.ingresado?.let { usuario ->
            viewModel.onNavegado()
            onIngresado(usuario)
        }
    }

    if (uiState.mostrarServidor) {
        AlertDialog(
            onDismissRequest = { viewModel.onMostrarServidor(false) },
            title = { Text(stringResource(R.string.titulo_servidor)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TextoSecundario(stringResource(R.string.texto_servidor))
                    CampoTexto(
                        valor = uiState.textoServidor,
                        onValorCambia = viewModel::onServidorCambia,
                        etiqueta = stringResource(R.string.campo_servidor),
                        icono = R.drawable.ic_ubicacion,
                        error = uiState.errorServidor,
                        tipoTeclado = KeyboardType.Uri
                    )
                    TextButton(onClick = viewModel::onRestablecerServidor) {
                        Text(stringResource(R.string.btn_servidor_por_defecto))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onGuardarServidor) { Text(stringResource(R.string.btn_guardar_servidor)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onMostrarServidor(false) }) { Text(stringResource(R.string.btn_cancelar)) }
            }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tarjeta),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = Dimens.espacioGrande)
                    .size(Dimens.tamanoIconoGrande)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = stringResource(R.string.login_subtitulo), style = MaterialTheme.typography.bodyMedium)

            TarjetaSeccion(titulo = stringResource(R.string.titulo_login)) {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = viewModel::onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
                CampoContrasena(
                    valor = uiState.contrasena,
                    onValorCambia = viewModel::onContrasenaCambia,
                    etiqueta = stringResource(R.string.campo_contrasena),
                    error = uiState.errorContrasena
                )
                if (uiState.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                BotonPrincipal(
                    texto = stringResource(R.string.btn_ingresar),
                    onClick = viewModel::onIngresar,
                    habilitado = !uiState.cargando,
                    icono = R.drawable.ic_candado,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = onRecuperar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.btn_olvide_contrasena))
                }
            }

            AnimatedVisibility(visible = uiState.error != null) {
                MensajeInformativo(
                    texto = uiState.error ?: "",
                    icono = R.drawable.ic_info,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }

            TextButton(onClick = { viewModel.onMostrarServidor(true) }) {
                Text(stringResource(R.string.servidor_actual, uiState.servidor))
            }

            TarjetaSeccion(titulo = stringResource(R.string.titulo_cuentas_prueba)) {
                TextoSecundario(stringResource(R.string.texto_cuentas_prueba))
                cuentasDePrueba.forEach { (correo, rol) ->
                    TextButton(onClick = {
                        viewModel.onCorreoCambia(correo)
                        viewModel.onContrasenaCambia("123456")
                    }) {
                        Text(stringResource(rol, correo))
                    }
                }
            }
        }
    }
}
