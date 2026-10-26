package cl.duocuc.pagoflex.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProgresoAnimado(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animacion = remember(progreso) { Animatable(0f) }
    LaunchedEffect(progreso) {
        animacion.animateTo(progreso, animationSpec = tween(durationMillis = 1000))
    }

    LinearProgressIndicator(
        progress = { animacion.value },
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    )
}
