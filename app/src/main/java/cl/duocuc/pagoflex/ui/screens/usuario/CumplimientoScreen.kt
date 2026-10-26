package cl.duocuc.pagoflex.ui.screens.usuario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.ui.components.BarraSuperior
import cl.duocuc.pagoflex.ui.components.ProgresoAnimado
import cl.duocuc.pagoflex.ui.components.TarjetaEstadistica
import cl.duocuc.pagoflex.ui.components.TarjetaSeccion
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.util.Formato

@Composable
fun CumplimientoScreen(
    cumplimiento: Cumplimiento,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.menu_cumplimiento), onVolver = onVolver) },
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
            TarjetaSeccion(titulo = stringResource(R.string.titulo_cumplimiento)) {
                Text(
                    text = cumplimiento.porcentaje?.let { Formato.porcentaje(it) } ?: "—",
                    style = MaterialTheme.typography.titleLarge
                )
                ProgresoAnimado(
                    progreso = ((cumplimiento.porcentaje ?: 0.0) / 100.0).toFloat(),
                    color = EstadoPagado
                )
                Text(text = cumplimiento.categoria.etiqueta, style = MaterialTheme.typography.titleMedium)
                Text(text = cumplimiento.categoria.mensaje, style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                TarjetaEstadistica(cumplimiento.aTiempo.toString(), stringResource(R.string.pagos_a_tiempo), Modifier.weight(1f))
                TarjetaEstadistica(cumplimiento.considerados.toString(), stringResource(R.string.compromisos_considerados), Modifier.weight(1f))
            }

            TarjetaSeccion(titulo = stringResource(R.string.como_se_calcula)) {
                Text(text = stringResource(R.string.explicacion_cumplimiento), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
