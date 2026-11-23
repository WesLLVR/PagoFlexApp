package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.AvisosPreferencias
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.logica.ReglasNegocio
import cl.duocuc.pagoflex.model.Aviso
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.FiltroCompromiso
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.ResumenMes
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InicioUiState(
    val nombre: String = "",
    val resumen: ResumenMes = ResumenMes(),
    val cumplimiento: Cumplimiento = Cumplimiento(),
    val avisos: List<Aviso> = emptyList(),
    val filtro: FiltroCompromiso = FiltroCompromiso.VIGENTES,
    val buscar: String = "",
    val compromisos: Pagina<Compromiso> = Pagina(),
    val cargando: Boolean = true,
    val error: String? = null
)

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val pagos = PagoRepository()
    private val sesion = SesionPreferencias(application)
    private val avisosPreferencias = AvisosPreferencias(application)
    private var trabajoLista: Job? = null

    private val _uiState = MutableStateFlow(InicioUiState(nombre = sesion.nombre()))
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun recargar() {
        val usuarioId = sesion.idUsuario()
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val configuracion = avisosPreferencias.leer()
            val resumen = compromisos.resumenMes(usuarioId)
            val proximos = compromisos.proximos(usuarioId, configuracion.diasAnticipacion)
            val cumplimiento = pagos.cumplimiento(usuarioId)
            _uiState.update {
                it.copy(
                    nombre = sesion.nombre(),
                    resumen = resumen,
                    cumplimiento = cumplimiento,
                    avisos = ReglasNegocio.avisos(proximos, configuracion),
                    error = null
                )
            }
            cargarLista(_uiState.value.compromisos.pagina)
        }
    }

    fun onFiltroSeleccionado(filtro: FiltroCompromiso) {
        _uiState.update { it.copy(filtro = filtro) }
        cargarLista(0)
    }

    fun onBuscarCambia(texto: String) {
        _uiState.update { it.copy(buscar = texto) }
        cargarLista(0)
    }

    fun onPaginaAnterior() = cargarLista(_uiState.value.compromisos.pagina - 1)

    fun onPaginaSiguiente() = cargarLista(_uiState.value.compromisos.pagina + 1)

    private fun cargarLista(pagina: Int) {
        val estado = _uiState.value
        trabajoLista?.cancel()
        trabajoLista = ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resultado = compromisos.listarDeUsuario(
                sesion.idUsuario(), estado.filtro.valorApi, estado.buscar, pagina.coerceAtLeast(0)
            )
            _uiState.update { it.copy(compromisos = resultado, cargando = false, error = null) }
        }
    }
}
