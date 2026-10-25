package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Pago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HistorialUiState(
    val pagos: List<Pago> = emptyList(),
    val anulados: List<Compromiso> = emptyList(),
    val empresas: List<String> = emptyList(),
    val anios: List<Int> = emptyList(),
    val empresaSeleccionada: String? = null,
    val anioSeleccionado: Int? = null
) {
    val pagosFiltrados: List<Pago>
        get() = pagos.filter {
            (empresaSeleccionada == null || it.empresa == empresaSeleccionada) &&
                (anioSeleccionado == null || it.fechaHora.year == anioSeleccionado)
        }

    val anuladosFiltrados: List<Compromiso>
        get() = anulados.filter {
            (empresaSeleccionada == null || it.empresa == empresaSeleccionada) &&
                (anioSeleccionado == null || it.vencimiento.year == anioSeleccionado)
        }
}

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val pagos = PagoRepository(db)
    private val compromisos = CompromisoRepository(db)
    private val idUsuario = SesionPreferencias(application).leer()

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        val listaPagos = pagos.historialDeUsuario(idUsuario)
        val anulados = compromisos.listarDeUsuario(idUsuario).filter { it.estado == EstadoCompromiso.ANULADO }
        _uiState.value = HistorialUiState(
            pagos = listaPagos,
            anulados = anulados,
            empresas = (listaPagos.map { it.empresa } + anulados.map { it.empresa }).distinct().sorted(),
            anios = listaPagos.map { it.fechaHora.year }.distinct().sortedDescending()
        )
    }

    fun onEmpresaSeleccionada(empresa: String?) {
        _uiState.update { it.copy(empresaSeleccionada = empresa) }
    }

    fun onAnioSeleccionado(anio: Int?) {
        _uiState.update { it.copy(anioSeleccionado = anio) }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
