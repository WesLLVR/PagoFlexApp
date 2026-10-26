package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AgenteUiState(
    val reportes: List<Reporte> = emptyList(),
    val filtro: EstadoReporte? = null,
    val respuesta: String = "",
    val errorRespuesta: String? = null
) {
    val reportesFiltrados: List<Reporte>
        get() = reportes
            .filter { filtro == null || it.estado == filtro }
            .sortedWith(compareByDescending<Reporte> { it.atrasado }.thenBy { it.estado.ordinal }.thenBy { it.fecha })

    val abiertos: Int get() = reportes.count { it.estado == EstadoReporte.ABIERTO }
    val atrasados: Int get() = reportes.count { it.atrasado }
    val enGestion: Int get() = reportes.count { it.estado == EstadoReporte.EN_GESTION }
}

class AgenteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val repository = ReporteRepository(db)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(AgenteUiState())
    val uiState: StateFlow<AgenteUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        _uiState.update { it.copy(reportes = repository.listar()) }
    }

    fun onFiltroSeleccionado(estado: EstadoReporte?) {
        _uiState.update { it.copy(filtro = estado) }
    }

    fun reporte(id: Long): Reporte? = _uiState.value.reportes.firstOrNull { it.id == id }

    fun prepararRespuesta() {
        _uiState.update { it.copy(respuesta = "", errorRespuesta = null) }
    }

    fun onRespuestaCambia(valor: String) {
        _uiState.update { it.copy(respuesta = valor, errorRespuesta = null) }
    }

    fun tomar(reporte: Reporte) {
        repository.tomar(reporte.id, sesion.leer())
        recargar()
    }

    fun resolver(reporte: Reporte): Boolean {
        val error = Validador.respuestaAgente(_uiState.value.respuesta)
        if (error != null) {
            _uiState.update { it.copy(errorRespuesta = error) }
            return false
        }
        repository.resolver(reporte, _uiState.value.respuesta)
        _uiState.update { it.copy(respuesta = "", errorRespuesta = null) }
        recargar()
        return true
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
