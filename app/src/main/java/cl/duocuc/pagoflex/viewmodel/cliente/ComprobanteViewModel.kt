package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ComprobanteUiState(
    val pago: Pago? = null,
    val siguienteCuota: Compromiso? = null,
    val cuotasPagadas: Int = 0,
    val totalCuotas: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null
)

class ComprobanteViewModel(application: Application) : AndroidViewModel(application) {

    private val pagos = PagoRepository()
    private val compromisos = CompromisoRepository()

    private val _uiState = MutableStateFlow(ComprobanteUiState())
    val uiState: StateFlow<ComprobanteUiState> = _uiState.asStateFlow()

    fun cargar(compromisoId: Long) {
        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val pago = pagos.pagoDeCompromiso(compromisoId)
                ?: throw Exception("Este compromiso todavía no tiene comprobante")
            val cuotas = compromisos.cuotas(compromisoId)
            _uiState.update {
                it.copy(
                    pago = pago,
                    siguienteCuota = cuotas.firstOrNull { cuota -> cuota.sePuedePagar },
                    cuotasPagadas = cuotas.count { cuota -> cuota.estado == EstadoCompromiso.PAGADO },
                    totalCuotas = cuotas.size,
                    cargando = false
                )
            }
        }
    }
}
