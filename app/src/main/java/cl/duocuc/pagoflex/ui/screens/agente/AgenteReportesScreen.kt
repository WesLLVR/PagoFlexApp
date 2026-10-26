package cl.duocuc.pagoflex.ui.screens.agente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.CampoSeleccion
import cl.duocuc.pagoflex.ui.components.EtiquetaEstado
import cl.duocuc.pagoflex.ui.components.MenuLateral
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.colorDeEstado
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.ErrorSuave
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.AgenteUiState
import kotlinx.coroutines.launch

@Composable
fun AgenteReportesScreen(
    usuario: Usuario?,
    uiState: AgenteUiState,
    onFiltroSeleccionado: (EstadoReporte?) -> Unit,
    onVerReporte: (Long) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val textoTodos = stringResource(R.string.filtro_todos)

    MenuLateral(
        drawerState = drawerState,
        nombre = usuario?.nombre ?: "",
        descripcion = stringResource(R.string.perfil_agente, usuario?.cargo ?: ""),
        opciones = emptyList(),
        onCerrarSesion = onCerrarSesion
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = stringResource(R.string.titulo_reportes),
                    onAbrirMenu = { scope.launch { drawerState.open() } }
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
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        TarjetaEstadistica(uiState.abiertos.toString(), stringResource(R.string.estado_abiertos), Modifier.weight(1f))
                        TarjetaEstadistica(uiState.atrasados.toString(), stringResource(R.string.estado_atrasados), Modifier.weight(1f))
                        TarjetaEstadistica(uiState.enGestion.toString(), stringResource(R.string.estado_en_gestion), Modifier.weight(1f))
                    }
                }
                item {
                    CampoSeleccion(
                        etiqueta = stringResource(R.string.campo_filtrar_estado),
                        opciones = listOf<EstadoReporte?>(null) + EstadoReporte.entries,
                        seleccionada = uiState.filtro,
                        onSeleccionar = onFiltroSeleccionado,
                        textoDe = { it?.etiqueta ?: textoTodos },
                        icono = R.drawable.ic_buscar
                    )
                }
                if (uiState.reportesFiltrados.isEmpty()) {
                    item { Text(stringResource(R.string.sin_reportes), style = MaterialTheme.typography.bodyMedium) }
                }
                items(uiState.reportesFiltrados, key = { it.id }) { reporte ->
                    TarjetaReporte(reporte = reporte, onClick = { onVerReporte(reporte.id) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaReporte(reporte: Reporte, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text("${reporte.numero}, ${Fechas.mostrar(reporte.fecha)}") },
            headlineContent = { Text(reporte.nombreUsuario) },
            supportingContent = { Text("${reporte.empresa}\n${reporte.motivo.etiqueta}") },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    EtiquetaEstado(texto = reporte.estado.etiqueta, color = colorDeEstado(reporte.estado))
                    if (reporte.atrasado) {
                        EtiquetaEstado(
                            texto = stringResource(R.string.etiqueta_atrasado),
                            color = ErrorSuave,
                            modifier = Modifier.padding(top = Dimens.espacioMini)
                        )
                    }
                }
            }
        )
    }
}
