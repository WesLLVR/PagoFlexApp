package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.util.Formato

@Composable
fun TarjetaCompromiso(
    compromiso: Compromiso,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarPersona: Boolean = false
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        ListItem(
            overlineContent = { Text(if (mostrarPersona) compromiso.nombreUsuario else compromiso.empresa) },
            headlineContent = { Text(Formato.pesos(compromiso.totalAPagar)) },
            supportingContent = {
                Text("${compromiso.concepto}\nVence el ${Fechas.mostrar(compromiso.vencimiento)}")
            },
            trailingContent = {
                EtiquetaEstado(texto = compromiso.estado.etiqueta, color = colorDeEstado(compromiso.estado))
            }
        )
    }
}
