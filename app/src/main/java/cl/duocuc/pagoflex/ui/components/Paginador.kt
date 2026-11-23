package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.model.Pagina

@Composable
fun Paginador(
    pagina: Pagina<*>,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pagina.totalElementos == 0L) return
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAnterior, enabled = pagina.pagina > 0) {
            Icon(painterResource(R.drawable.ic_anterior), contentDescription = stringResource(R.string.cd_pagina_anterior))
        }
        Text(
            text = stringResource(
                R.string.texto_pagina,
                pagina.pagina + 1,
                pagina.totalPaginas,
                pagina.totalElementos
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSiguiente, enabled = pagina.pagina < pagina.totalPaginas - 1) {
            Icon(painterResource(R.drawable.ic_siguiente), contentDescription = stringResource(R.string.cd_pagina_siguiente))
        }
    }
}
