package cl.duocuc.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.ui.theme.Dimens

@Composable
fun IngresoScreen(
    sesiones: List<Usuario>,
    onIngresar: (Usuario) -> Unit
) {
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
            Text(text = stringResource(R.string.ingreso_subtitulo), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.ingreso_pregunta),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.espacioMedio)
            )
            sesiones.forEach { usuario ->
                TarjetaPerfil(usuario = usuario, onClick = { onIngresar(usuario) })
            }
        }
    }
}

@Composable
private fun TarjetaPerfil(usuario: Usuario, onClick: () -> Unit) {
    val icono = when (usuario.rol) {
        Rol.USUARIO_FINAL -> R.drawable.ic_persona
        Rol.AGENTE -> R.drawable.ic_soporte
        Rol.EJECUTIVO -> R.drawable.ic_empresa
    }
    val detalle = when (usuario.rol) {
        Rol.EJECUTIVO -> stringResource(R.string.perfil_ejecutivo, usuario.cargo ?: "", usuario.nombreEmpresa ?: "")
        Rol.AGENTE -> stringResource(R.string.perfil_agente, usuario.cargo ?: "")
        Rol.USUARIO_FINAL -> stringResource(R.string.perfil_usuario)
    }

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(painterResource(icono), contentDescription = null) },
            headlineContent = { Text(usuario.nombre) },
            supportingContent = { Text(detalle) }
        )
    }
}
