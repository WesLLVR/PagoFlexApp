package cl.duocuc.pagoflex.ui.screens.ejecutivo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.FiltroCompromiso
import cl.duocuc.pagoflex.ui.components.AvisoError
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoBusqueda
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.FiltroChips
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.OpcionMenu
import cl.duocuc.pagoflex.ui.components.Paginador
import cl.duocuc.pagoflex.ui.components.PantallaCargando
import cl.duocuc.pagoflex.ui.components.RecargaAutomatica
import cl.duocuc.pagoflex.ui.components.TarjetaCompromiso
import cl.duocuc.pagoflex.ui.components.TextoSecundario
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Formato
import cl.duocuc.pagoflex.viewmodel.ejecutivo.EjecutivoCompromisosViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun EjecutivoCompromisosScreen(
    onVerCompromiso: (Long) -> Unit,
    onRegistrar: () -> Unit,
    onVerResumen: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: EjecutivoCompromisosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val textoTodos = stringResource(R.string.filtro_todos)

    RecargaAutomatica(onRecargar = viewModel::recargar)

    MenuLateral(
        drawerState = drawerState,
        nombre = uiState.nombre,
        descripcion = stringResource(R.string.perfil_ejecutivo, uiState.empresa),
        opciones = listOf(
            OpcionMenu(stringResource(R.string.menu_registrar), R.drawable.ic_agregar, onRegistrar),
            OpcionMenu(stringResource(R.string.menu_resumen), R.drawable.ic_grafico, onVerResumen),
            OpcionMenu(stringResource(R.string.menu_perfil), R.drawable.ic_persona, onVerPerfil)
        ),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = uiState.empresa.ifEmpty { stringResource(R.string.titulo_compromisos_empresa) },
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
                    FiltroChips(
                        opciones = FiltroCompromiso.entries.filter { it != FiltroCompromiso.VIGENTES },
                        seleccionada = uiState.filtro,
                        onSeleccionar = viewModel::onFiltroSeleccionado,
                        textoDe = { it.etiqueta }
                    )
                }
                item {
                    CampoSeleccion(
                        etiqueta = stringResource(R.string.campo_mes),
                        opciones = listOf<YearMonth?>(null) + uiState.meses,
                        seleccionada = uiState.mes,
                        onSeleccionar = viewModel::onMesSeleccionado,
                        textoDe = { mes -> mes?.let { Formato.mes(it) } ?: textoTodos },
                        icono = R.drawable.ic_calendario,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    CampoBusqueda(
                        texto = uiState.buscar,
                        onTextoCambia = viewModel::onBuscarCambia,
                        sugerencia = stringResource(R.string.buscar_compromisos_empresa)
                    )
                }
                when {
                    uiState.cargando -> item { PantallaCargando() }
                    uiState.error != null && uiState.compromisos.contenido.isEmpty() ->
                        item { AvisoError(uiState.error ?: "", viewModel::recargar) }
                    uiState.compromisos.contenido.isEmpty() -> item { TextoSecundario(stringResource(R.string.sin_resultados)) }
                }
                items(uiState.compromisos.contenido, key = { it.id }) { compromiso ->
                    TarjetaCompromiso(
                        compromiso = compromiso,
                        onClick = { onVerCompromiso(compromiso.id) },
                        mostrarPersona = true
                    )
                }
                item {
                    Paginador(
                        pagina = uiState.compromisos,
                        onAnterior = viewModel::onPaginaAnterior,
                        onSiguiente = viewModel::onPaginaSiguiente
                    )
                }
                item { Spacer(Modifier.height(Dimens.espacioFab)) }
            }
        }
    }
}
