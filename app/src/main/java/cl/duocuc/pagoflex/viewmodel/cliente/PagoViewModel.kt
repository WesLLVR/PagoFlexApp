package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PagoUiState(
    val compromiso: Compromiso? = null,
    val motivoNoPagable: String? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val medio: MedioPago? = null,
    val errorMedio: String? = null,
    val mostrarConfirmacion: Boolean = false,
    val procesando: Boolean = false,
    val pagado: Boolean = false,
    val mensaje: String? = null
)

class PagoViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val pagos = PagoRepository()

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun cargar(id: Long) {
        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val compromiso = compromisos.obtener(id)
            val motivo = if (compromiso.sePuedePagar) compromisos.motivoNoPagable(id) else "Este compromiso ya no se puede pagar."
            _uiState.update { it.copy(compromiso = compromiso, motivoNoPagable = motivo, cargando = false) }
        }
    }

    fun onMedioSeleccionado(medio: MedioPago) = _uiState.update { it.copy(medio = medio, errorMedio = null) }

    fun onSolicitarPago() {
        val error = Validador.medioPago(_uiState.value.medio)
        _uiState.update { it.copy(errorMedio = error, mostrarConfirmacion = error == null) }
    }

    fun onCancelar() = _uiState.update { it.copy(mostrarConfirmacion = false) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onConfirmar() {
        val estado = _uiState.value
        val compromiso = estado.compromiso ?: return
        val medio = estado.medio ?: return
        _uiState.update { it.copy(mostrarConfirmacion = false, procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            pagos.pagar(compromiso.id, medio)
            _uiState.update { it.copy(procesando = false, pagado = true) }
        }
    }
}
