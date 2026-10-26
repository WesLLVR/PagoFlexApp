package cl.duocuc.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.theme.Dimens
import kotlinx.coroutines.launch

data class OpcionMenu(
    val texto: String,
    @DrawableRes val icono: Int,
    val onClick: () -> Unit
)

@Composable
fun MenuLateral(
    drawerState: DrawerState,
    nombre: String,
    descripcion: String,
    opciones: List<OpcionMenu>,
    onCerrarSesion: () -> Unit,
    contenido: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_salir_titulo)) },
            text = { Text(stringResource(R.string.dialogo_salir_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()
                }) { Text(stringResource(R.string.btn_cambiar_perfil)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = Dimens.espacioMedio, top = Dimens.espacioGrande, end = Dimens.espacioMedio)
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
                )
                HorizontalDivider()

                opciones.forEach { opcion ->
                    NavigationDrawerItem(
                        label = { Text(opcion.texto) },
                        icon = { Icon(painterResource(opcion.icono), contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            opcion.onClick()
                        },
                        modifier = Modifier.padding(horizontal = Dimens.espacioChico)
                    )
                }

                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.btn_cambiar_perfil)) },
                    icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogo = true
                    },
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico)
                )
            }
        }
    ) {
        contenido()
    }
}
