package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.ui.theme.Dimens
import coil3.compose.AsyncImage

@Composable
fun AvatarCircular(
    fotoUrl: String?,
    descripcion: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar
) {
    val forma = modifier
        .size(tamano)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.primary, CircleShape)
    if (fotoUrl != null) {
        AsyncImage(
            model = fotoUrl,
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_persona),
            error = painterResource(R.drawable.ic_persona),
            modifier = forma
        )
    } else {
        Box(modifier = forma, contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_persona),
                contentDescription = descripcion,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(tamano)
                    .padding(tamano / 5)
            )
        }
    }
}
