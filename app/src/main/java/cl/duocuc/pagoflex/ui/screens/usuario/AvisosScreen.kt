package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.GrupoRadio
import cl.duocuc.pagoflex.ui.components.InterruptorConTexto
import cl.duocuc.pagoflex.ui.components.MensajeInformativo
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Advertencia
import cl.duocuc.pagoflex.ui.theme.AdvertenciaFondo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.Exito
import cl.duocuc.pagoflex.ui.theme.ExitoFondo
import cl.duocuc.pagoflex.viewmodel.AvisosViewModel

@Composable
fun AvisosScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: AvisosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_avisos), onVolver = onVolver) },
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
            Text(text = stringResource(R.string.avisos_descripcion), style = MaterialTheme.typography.bodyLarge)

            TarjetaSeccion(titulo = stringResource(R.string.titulo_preferencias_avisos)) {
                InterruptorConTexto(
                    texto = stringResource(R.string.opcion_recibir_avisos),
                    activado = uiState.activos,
                    onCambio = viewModel::onActivosCambia
                )
                AnimatedVisibility(visible = uiState.activos) {
                    Column {
                        Text(text = stringResource(R.string.etiqueta_anticipacion), style = MaterialTheme.typography.bodyMedium)
                        GrupoRadio(
                            opciones = uiState.opcionesDias,
                            seleccionada = uiState.diasAnticipacion,
                            onSeleccionar = viewModel::onDiasSeleccionados,
                            textoDe = { dias -> "$dias ${if (dias == 1) "día" else "días"} antes" }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = uiState.activos) {
                MensajeInformativo(
                    texto = stringResource(R.string.ejemplo_aviso, uiState.diasAnticipacion),
                    icono = R.drawable.ic_aviso,
                    colorFondo = AdvertenciaFondo,
                    colorTexto = Advertencia
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar),
                onClick = {
                    viewModel.onGuardar()
                    onGuardado()
                },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = uiState.guardado) {
                MensajeInformativo(
                    texto = stringResource(R.string.avisos_guardados),
                    icono = R.drawable.ic_check,
                    colorFondo = ExitoFondo,
                    colorTexto = Exito
                )
            }
        }
    }
}
