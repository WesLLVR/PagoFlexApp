package cl.duocuc.pagoflex.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

private const val INTERVALO_RECARGA = 10_000L

@Composable
fun RecargaAutomatica(onRecargar: () -> Unit) {
    val recargar by rememberUpdatedState(onRecargar)
    val ciclo = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        ciclo.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                recargar()
                delay(INTERVALO_RECARGA)
            }
        }
    }
}
