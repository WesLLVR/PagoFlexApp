package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MedioPago
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PagoUiState(
    val medio: MedioPago? = null,
    val errorMedio: String? = null,
    val mostrarConfirmacion: Boolean = false,
    val procesando: Boolean = false,
    val pagoConfirmado: Boolean = false
)

class PagoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val repository = PagoRepository(db)

    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun onMedioSeleccionado(medio: MedioPago) {
        _uiState.update { it.copy(medio = medio, errorMedio = null) }
    }

    fun onSolicitarPago() {
        val error = Validador.medioPago(_uiState.value.medio)
        _uiState.update { it.copy(errorMedio = error, mostrarConfirmacion = error == null) }
    }

    fun onCancelar() {
        _uiState.update { it.copy(mostrarConfirmacion = false) }
    }

    fun onConfirmar(compromiso: Compromiso) {
        val medio = _uiState.value.medio ?: return
        _uiState.update { it.copy(mostrarConfirmacion = false, procesando = true) }
        viewModelScope.launch {
            delay(1500)
            repository.pagar(compromiso, medio)
            _uiState.update { it.copy(procesando = false, pagoConfirmado = true) }
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
