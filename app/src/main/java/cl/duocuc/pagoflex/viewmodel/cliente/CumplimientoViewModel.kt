package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CumplimientoUiState(
    val cumplimiento: Cumplimiento = Cumplimiento(),
    val cargando: Boolean = true,
    val error: String? = null
)

class CumplimientoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = PagoRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(CumplimientoUiState())
    val uiState: StateFlow<CumplimientoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val cumplimiento = repositorio.cumplimiento(sesion.idUsuario())
            _uiState.update { it.copy(cumplimiento = cumplimiento, cargando = false) }
        }
    }
}
