package cl.duocuc.pagoflex.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DatoGrafico(
    val etiqueta: String,
    val valor: Int,
    val color: Color
)

@Composable
fun GraficoBarras(
    datos: List<DatoGrafico>,
    modifier: Modifier = Modifier
) {
    val medidorTexto = rememberTextMeasurer()
    val colorTexto = MaterialTheme.colorScheme.onSurface
    val colorEje = MaterialTheme.colorScheme.outline

    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (datos.isEmpty()) return@Canvas

        val margenSuperior = 24.dp.toPx()
        val margenInferior = 28.dp.toPx()
        val altoDisponible = size.height - margenSuperior - margenInferior
        val baseY = size.height - margenInferior

        val valorMaximo = datos.maxOf { it.valor }.coerceAtLeast(1)
        val anchoColumna = size.width / datos.size
        val anchoBarra = anchoColumna * 0.55f

        drawLine(
            color = colorEje,
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx()
        )

        datos.forEachIndexed { indice, dato ->
            val altoBarra = altoDisponible * (dato.valor.toFloat() / valorMaximo) * progreso.value
            val inicioColumna = indice * anchoColumna
            val x = inicioColumna + (anchoColumna - anchoBarra) / 2
            val y = baseY - altoBarra

            drawRoundRect(
                color = dato.color,
                topLeft = Offset(x, y),
                size = Size(anchoBarra, altoBarra),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            val textoValor = medidorTexto.measure(
                text = dato.valor.toString(),
                style = TextStyle(fontSize = 13.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoValor,
                topLeft = Offset(
                    inicioColumna + (anchoColumna - textoValor.size.width) / 2,
                    y - textoValor.size.height - 2.dp.toPx()
                )
            )

            val textoEtiqueta = medidorTexto.measure(
                text = dato.etiqueta,
                style = TextStyle(fontSize = 12.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoEtiqueta,
                topLeft = Offset(
                    inicioColumna + (anchoColumna - textoEtiqueta.size.width) / 2,
                    baseY + 6.dp.toPx()
                )
            )
        }
    }
}
