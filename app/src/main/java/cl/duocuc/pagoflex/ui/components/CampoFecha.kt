package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.styles.EstilosCampo
import cl.duocuc.pagoflex.ui.theme.Dimens
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onFechaElegida: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    soloDesdeHoy: Boolean = false,
    permiteBorrar: Boolean = false
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = fecha?.let { Fechas.mostrar(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_calendario),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            singleLine = true,
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { abierto = true }
        )
    }

    if (abierto) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fecha?.let { Fechas.aMilisegundos(it) },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !soloDesdeHoy || !Fechas.desdeMilisegundos(utcTimeMillis).isBefore(Fechas.hoy())
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { onFechaElegida(Fechas.desdeMilisegundos(it)) }
                    abierto = false
                }) { Text(stringResource(R.string.btn_aceptar)) }
            },
            dismissButton = {
                if (permiteBorrar) {
                    TextButton(onClick = {
                        onFechaElegida(null)
                        abierto = false
                    }) { Text(stringResource(R.string.btn_borrar)) }
                } else {
                    TextButton(onClick = { abierto = false }) { Text(stringResource(R.string.btn_cancelar)) }
                }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
