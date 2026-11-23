package cl.duocuc.pagoflex.ui.screens.cuenta

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.AvatarCircular
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonesFoto
import cl.duocuc.pagoflex.ui.components.CampoContrasena
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.viewmodel.cuenta.PerfilViewModel

@Composable
fun PerfilScreen(
    onVolver: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_perfil), onVolver = onVolver) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val usuario = uiState.usuario
        when {
            uiState.cargando && usuario == null -> PantallaCargando(Modifier.padding(innerPadding))
            usuario == null -> AvisoError(
                mensaje = uiState.errorCarga ?: "",
                onReintentar = viewModel::cargar,
                modifier = Modifier.padding(innerPadding)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                if (uiState.procesando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                TarjetaSeccion(titulo = usuario.nombre) {
                    AvatarCircular(
                        fotoUrl = usuario.avatarUrl,
                        descripcion = stringResource(R.string.cd_foto_perfil),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    FilaDato(stringResource(R.string.dato_rol), usuario.rol.etiqueta)
                    FilaDato(stringResource(R.string.dato_rut), usuario.rut)
                    usuario.cargo?.let { FilaDato(stringResource(R.string.dato_cargo), it) }
                    BotonesFoto(
                        snackbarHostState = snackbarHostState,
                        onFotoTomada = viewModel::onFotoTomada,
                        onImagenElegida = viewModel::onImagenElegida,
                        habilitado = !uiState.procesando
                    )
                    if (usuario.avatarUrl != null) {
                        TextButton(
                            onClick = viewModel::onQuitarFoto,
                            enabled = !uiState.procesando,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) { Text(stringResource(R.string.btn_quitar_foto)) }
                    }
                }

                TarjetaSeccion(titulo = stringResource(R.string.titulo_mis_datos)) {
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
                        ayuda = stringResource(R.string.ayuda_opcional),
                        tipoTeclado = KeyboardType.Phone
                    )
                    CampoTexto(
                        valor = uiState.comuna,
                        onValorCambia = viewModel::onComunaCambia,
                        etiqueta = stringResource(R.string.campo_comuna),
                        icono = R.drawable.ic_ubicacion,
                        ayuda = stringResource(R.string.ayuda_opcional)
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_guardar_datos),
                        onClick = viewModel::onGuardarDatos,
                        habilitado = !uiState.procesando,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TarjetaSeccion(titulo = stringResource(R.string.titulo_cambiar_contrasena)) {
                    CampoContrasena(
                        valor = uiState.actual,
                        onValorCambia = viewModel::onActualCambia,
                        etiqueta = stringResource(R.string.campo_contrasena_actual),
                        error = uiState.errorActual
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
                        texto = stringResource(R.string.btn_cambiar_contrasena),
                        onClick = viewModel::onCambiarContrasena,
                        habilitado = !uiState.procesando,
                        icono = R.drawable.ic_candado,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}
