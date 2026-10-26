package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.ui.theme.EstadoAnulado
import cl.duocuc.pagoflex.ui.theme.EstadoPagado
import cl.duocuc.pagoflex.ui.theme.EstadoPendiente
import cl.duocuc.pagoflex.ui.theme.EstadoRevision
import cl.duocuc.pagoflex.ui.theme.EstadoVencido

fun colorDeEstado(estado: EstadoCompromiso): Color = when (estado) {
    EstadoCompromiso.PENDIENTE -> EstadoPendiente
    EstadoCompromiso.VENCIDO -> EstadoVencido
    EstadoCompromiso.PAGADO -> EstadoPagado
    EstadoCompromiso.EN_REVISION -> EstadoRevision
    EstadoCompromiso.ANULADO -> EstadoAnulado
}

fun colorDeEstado(estado: EstadoReporte): Color = when (estado) {
    EstadoReporte.ABIERTO -> EstadoVencido
    EstadoReporte.EN_GESTION -> EstadoRevision
    EstadoReporte.RESUELTO -> EstadoPagado
}

@Composable
fun EtiquetaEstado(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
        )
    }
}
