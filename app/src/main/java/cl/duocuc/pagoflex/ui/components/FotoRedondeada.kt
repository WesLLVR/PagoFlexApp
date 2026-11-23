package cl.duocuc.pagoflex.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cl.duocuc.pagoflex.ui.theme.Dimens
import coil3.compose.AsyncImage

@Composable
fun FotoRedondeada(
    imagen: Any,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(Dimens.radioFoto)
    AsyncImage(
        model = imagen,
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(Dimens.tamanoFoto)
            .shadow(elevation = 4.dp, shape = forma)
            .clip(forma)
            .border(Dimens.bordeFoto, MaterialTheme.colorScheme.primary, forma)
    )
}
