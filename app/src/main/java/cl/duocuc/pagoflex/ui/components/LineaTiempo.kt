package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.pagoflex.model.CambioEstado
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas

@Composable
fun LineaTiempo(
    cambios: List<CambioEstado>,
    estadoActual: EstadoReporte,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BarraEtapas(estadoActual)
        cambios.forEachIndexed { indice, cambio ->
            Row(modifier = Modifier.padding(top = Dimens.espacioChico)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(colorDeEstado(cambio.estado), CircleShape)
                    )
                    if (indice < cambios.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(44.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = Dimens.espacioChico)) {
                    Text(text = cambio.estado.etiqueta, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${Fechas.mostrar(cambio.fecha)} · ${cambio.autor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!cambio.comentario.isNullOrBlank()) {
                        Text(text = cambio.comentario, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraEtapas(estadoActual: EstadoReporte) {
    val etapas = EstadoReporte.entries
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMini),
        verticalAlignment = Alignment.CenterVertically
    ) {
        etapas.forEach { etapa ->
            val alcanzada = etapa.ordinal <= estadoActual.ordinal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            if (alcanzada) colorDeEstado(etapa) else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                )
                Text(
                    text = etapa.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alcanzada) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
