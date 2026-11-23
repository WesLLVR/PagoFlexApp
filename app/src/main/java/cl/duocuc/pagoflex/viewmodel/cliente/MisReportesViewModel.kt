package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MisReportesUiState(
    val filtro: EstadoReporte? = null,
    val reportes: Pagina<Reporte> = Pagina(),
    val cargando: Boolean = true,
    val error: String? = null
)

class MisReportesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ReporteRepository()
    private val sesion = SesionPreferencias(application)
    private var trabajo: Job? = null

    private val _uiState = MutableStateFlow(MisReportesUiState())
    val uiState: StateFlow<MisReportesUiState> = _uiState.asStateFlow()

    fun recargar() = cargar(_uiState.value.reportes.pagina)

    fun onFiltroSeleccionado(filtro: EstadoReporte?) {
        _uiState.update { it.copy(filtro = filtro) }
        cargar(0)
    }

    fun onPaginaAnterior() = cargar(_uiState.value.reportes.pagina - 1)

    fun onPaginaSiguiente() = cargar(_uiState.value.reportes.pagina + 1)

    private fun cargar(pagina: Int) {
        val filtro = _uiState.value.filtro
        trabajo?.cancel()
        trabajo = ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resultado = repositorio.listar(
                estado = filtro,
                usuarioId = sesion.idUsuario(),
                agenteId = null,
                buscar = "",
                soloAtrasados = false,
                pagina = pagina.coerceAtLeast(0)
            )
            _uiState.update { it.copy(reportes = resultado, cargando = false, error = null) }
        }
    }
}
