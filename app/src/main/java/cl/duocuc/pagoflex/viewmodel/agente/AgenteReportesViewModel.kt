package cl.duocuc.pagoflex.viewmodel.agente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.ResumenReportes
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AgenteReportesUiState(
    val nombre: String = "",
    val resumen: ResumenReportes = ResumenReportes(),
    val filtro: EstadoReporte? = null,
    val soloMios: Boolean = false,
    val soloAtrasados: Boolean = false,
    val buscar: String = "",
    val reportes: Pagina<Reporte> = Pagina(),
    val cargando: Boolean = true,
    val error: String? = null
)

class AgenteReportesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ReporteRepository()
    private val sesion = SesionPreferencias(application)
    private var trabajoLista: Job? = null

    private val _uiState = MutableStateFlow(AgenteReportesUiState(nombre = sesion.nombre()))
    val uiState: StateFlow<AgenteReportesUiState> = _uiState.asStateFlow()

    fun recargar() {
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resumen = repositorio.resumen()
            _uiState.update { it.copy(resumen = resumen, nombre = sesion.nombre()) }
            cargarLista(_uiState.value.reportes.pagina)
        }
    }

    fun onFiltroSeleccionado(filtro: EstadoReporte?) {
        _uiState.update { it.copy(filtro = filtro) }
        cargarLista(0)
    }

    fun onSoloMiosCambia(valor: Boolean) {
        _uiState.update { it.copy(soloMios = valor) }
        cargarLista(0)
    }

    fun onSoloAtrasadosCambia(valor: Boolean) {
        _uiState.update { it.copy(soloAtrasados = valor) }
        cargarLista(0)
    }

    fun onBuscarCambia(texto: String) {
        _uiState.update { it.copy(buscar = texto) }
        cargarLista(0)
    }

    fun onPaginaAnterior() = cargarLista(_uiState.value.reportes.pagina - 1)

    fun onPaginaSiguiente() = cargarLista(_uiState.value.reportes.pagina + 1)

    private fun cargarLista(pagina: Int) {
        val estado = _uiState.value
        trabajoLista?.cancel()
        trabajoLista = ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resultado = repositorio.listar(
                estado = estado.filtro,
                usuarioId = null,
                agenteId = if (estado.soloMios) sesion.idUsuario() else null,
                buscar = estado.buscar,
                soloAtrasados = estado.soloAtrasados,
                pagina = pagina.coerceAtLeast(0)
            )
            _uiState.update { it.copy(reportes = resultado, cargando = false, error = null) }
        }
    }
}
