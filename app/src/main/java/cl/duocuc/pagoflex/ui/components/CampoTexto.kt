package cl.duocuc.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.styles.EstilosCampo
import cl.duocuc.pagoflex.ui.theme.Dimens

@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    lineas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        trailingIcon = if (error != null) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = error,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            }
        } else null,
        isError = error != null,
        supportingText = {
            if (error != null) Text(error) else if (ayuda != null) Text(ayuda)
        },
        singleLine = lineas == 1,
        minLines = lineas,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = if (lineas == 1) ImeAction.Next else ImeAction.Default
        ),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}
