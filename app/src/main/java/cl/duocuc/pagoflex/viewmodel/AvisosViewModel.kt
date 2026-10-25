package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.AvisosPreferencias
import cl.duocuc.pagoflex.model.ConfiguracionAvisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AvisosUiState(
    val activos: Boolean = true,
    val diasAnticipacion: Int = 3,
    val opcionesDias: List<Int> = listOf(1, 3, 5),
    val guardado: Boolean = false
)

class AvisosViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = AvisosPreferencias(application)

    private val _uiState = MutableStateFlow(AvisosUiState())
    val uiState: StateFlow<AvisosUiState> = _uiState.asStateFlow()

    init {
        val actual = preferencias.leer()
        _uiState.value = AvisosUiState(activos = actual.activos, diasAnticipacion = actual.diasAnticipacion)
    }

    fun onActivosCambia(valor: Boolean) {
        _uiState.update { it.copy(activos = valor, guardado = false) }
    }

    fun onDiasSeleccionados(dias: Int) {
        _uiState.update { it.copy(diasAnticipacion = dias, guardado = false) }
    }

    fun onGuardar() {
        val estado = _uiState.value
        preferencias.guardar(ConfiguracionAvisos(estado.activos, estado.diasAnticipacion))
        _uiState.update { it.copy(guardado = true) }
    }
}
