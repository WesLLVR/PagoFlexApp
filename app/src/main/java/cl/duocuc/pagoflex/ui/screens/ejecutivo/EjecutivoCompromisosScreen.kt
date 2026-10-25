package cl.duocuc.pagoflex.ui.screens.ejecutivo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.OpcionMenu
import cl.duocuc.pagoflex.ui.components.TarjetaCompromiso
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.EjecutivoUiState
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun EjecutivoCompromisosScreen(
    usuario: Usuario?,
    uiState: EjecutivoUiState,
    onFiltroSeleccionado: (EstadoCompromiso?) -> Unit,
    onFiltroMesSeleccionado: (YearMonth?) -> Unit,
    onVerCompromiso: (Long) -> Unit,
    onRegistrar: () -> Unit,
    onVerResumen: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val textoTodos = stringResource(R.string.filtro_todos)

    MenuLateral(
        drawerState = drawerState,
        nombre = usuario?.nombre ?: "",
        descripcion = stringResource(R.string.perfil_ejecutivo, usuario?.cargo ?: "", usuario?.nombreEmpresa ?: ""),
        opciones = listOf(
            OpcionMenu(stringResource(R.string.menu_registrar), R.drawable.ic_agregar, onRegistrar),
            OpcionMenu(stringResource(R.string.menu_resumen), R.drawable.ic_grafico, onVerResumen)
        ),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = uiState.empresa?.nombre ?: stringResource(R.string.titulo_compromisos_empresa),
                    onAbrirMenu = { scope.launch { drawerState.open() } }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onRegistrar,
                    icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                    text = { Text(stringResource(R.string.btn_nuevo)) },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        CampoSeleccion(
                            etiqueta = stringResource(R.string.campo_estado),
                            opciones = listOf<EstadoCompromiso?>(null) + EstadoCompromiso.entries,
                            seleccionada = uiState.filtro,
                            onSeleccionar = onFiltroSeleccionado,
                            textoDe = { it?.etiqueta ?: textoTodos },
                            icono = R.drawable.ic_buscar,
                            modifier = Modifier.weight(1f)
                        )
                        CampoSeleccion(
                            etiqueta = stringResource(R.string.campo_mes),
                            opciones = listOf<YearMonth?>(null) + uiState.mesesDisponibles,
                            seleccionada = uiState.filtroMes,
                            onSeleccionar = onFiltroMesSeleccionado,
                            textoDe = { mes -> mes?.let { Formato.mes(it) } ?: textoTodos },
                            icono = R.drawable.ic_calendario,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.cantidad_compromisos, uiState.compromisosFiltrados.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                items(uiState.compromisosFiltrados, key = { it.id }) { compromiso ->
                    TarjetaCompromiso(
                        compromiso = compromiso,
                        onClick = { onVerCompromiso(compromiso.id) },
                        mostrarPersona = true
                    )
                }
                item { Spacer(Modifier.height(Dimens.espacioFab)) }
            }
        }
    }
}
