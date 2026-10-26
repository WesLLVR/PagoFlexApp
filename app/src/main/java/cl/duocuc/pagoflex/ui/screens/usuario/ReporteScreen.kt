package cl.duocuc.pagoflex.ui.screens.usuario

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.permisos.Permisos
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.BotonPrincipal
import cl.duocuc.pagoflex.ui.components.BotonSecundario
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.CampoTexto
import cl.duocuc.pagoflex.ui.components.FilaDato
import cl.duocuc.pagoflex.ui.components.FotoRedondeada
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.ReporteViewModel
import kotlinx.coroutines.launch

@Composable
fun ReporteScreen(
    compromiso: Compromiso,
    onVolver: () -> Unit,
    onEnviado: () -> Unit,
    viewModel: ReporteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)
    val textoSinApp = stringResource(R.string.sin_app_disponible)

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) viewModel.onFotoTomada(foto)
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar(textoSinApp) }
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamaraDenegada)
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) viewModel.onImagenElegida(uri)
    }

    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            lanzadorGaleria.launch(intent)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar(textoSinApp) }
        }
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado(textoGaleriaDenegada)
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.titulo_reportar), onVolver = onVolver) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_camara),
                        onClick = {
                            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                            else pedirPermisoCamara.launch(Permisos.CAMARA)
                        },
                        icono = R.drawable.ic_camara,
                        modifier = Modifier.weight(1f)
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_galeria),
                        onClick = {
                            if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
                            else pedirPermisoImagenes.launch(Permisos.IMAGENES)
                        },
                        icono = R.drawable.ic_galeria,
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.errorFoto?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                AnimatedVisibility(
                    visible = uiState.foto != null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        uiState.foto?.let { bytes ->
                            FotoRedondeada(bytes = bytes, descripcion = stringResource(R.string.cd_foto_comprobante))
                        }
                        TextButton(onClick = viewModel::onQuitarFoto) {
                            Text(stringResource(R.string.btn_quitar_foto))
                        }
                    }
                }
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_enviar_reporte),
                onClick = { if (viewModel.onEnviar(compromiso.id)) onEnviado() },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
