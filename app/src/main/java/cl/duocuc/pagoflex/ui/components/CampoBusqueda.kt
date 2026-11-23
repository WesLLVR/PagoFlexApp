package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.styles.EstilosCampo
import cl.duocuc.pagoflex.ui.theme.Dimens

@Composable
fun CampoBusqueda(
    texto: String,
    onTextoCambia: (String) -> Unit,
    sugerencia: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambia,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(sugerencia) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_buscar),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        trailingIcon = if (texto.isNotEmpty()) {
            {
                IconButton(onClick = { onTextoCambia("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_eliminar),
                        contentDescription = stringResource(R.string.cd_limpiar_busqueda),
                        modifier = Modifier.size(Dimens.tamanoIcono)
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}
