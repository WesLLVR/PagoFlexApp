package cl.duocuc.pagoflex.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.permisos.Permisos
import cl.duocuc.pagoflex.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun BotonesFoto(
    snackbarHostState: SnackbarHostState,
    onFotoTomada: (Bitmap) -> Unit,
    onImagenElegida: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val context = LocalContext.current
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
        if (foto != null) onFotoTomada(foto)
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
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) onImagenElegida(uri)
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

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        BotonSecundario(
            texto = stringResource(R.string.btn_camara),
            onClick = {
                if (!habilitado) return@BotonSecundario
                if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                else pedirPermisoCamara.launch(Permisos.CAMARA)
            },
            icono = R.drawable.ic_camara,
            modifier = Modifier.weight(1f)
        )
        BotonSecundario(
            texto = stringResource(R.string.btn_galeria),
            onClick = {
                if (!habilitado) return@BotonSecundario
                if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
                else pedirPermisoImagenes.launch(Permisos.IMAGENES)
            },
            icono = R.drawable.ic_galeria,
            modifier = Modifier.weight(1f)
        )
    }
}
