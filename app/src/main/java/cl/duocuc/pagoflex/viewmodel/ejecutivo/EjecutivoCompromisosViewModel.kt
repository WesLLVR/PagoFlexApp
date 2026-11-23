package cl.duocuc.pagoflex.viewmodel.ejecutivo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.FiltroCompromiso
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth

data class EjecutivoCompromisosUiState(
    val nombre: String = "",
    val empresa: String = "",
    val filtro: FiltroCompromiso = FiltroCompromiso.TODOS,
    val mes: YearMonth? = null,
    val meses: List<YearMonth> = emptyList(),
    val buscar: String = "",
    val compromisos: Pagina<Compromiso> = Pagina(),
    val cargando: Boolean = true,
    val error: String? = null
)

class EjecutivoCompromisosViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = CompromisoRepository()
    private val sesion = SesionPreferencias(application)
    private var trabajoLista: Job? = null

    private val _uiState = MutableStateFlow(
        EjecutivoCompromisosUiState(
            nombre = sesion.nombre(),
            meses = (-12L..6L).map { YearMonth.from(Fechas.hoy()).plusMonths(it) }.reversed()
        )
    )
    val uiState: StateFlow<EjecutivoCompromisosUiState> = _uiState.asStateFlow()

    fun recargar() {
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            if (_uiState.value.empresa.isEmpty()) {
                val empresa = repositorio.empresa(sesion.empresaId())
                _uiState.update { it.copy(empresa = empresa.nombre) }
            }
            cargarLista(_uiState.value.compromisos.pagina)
        }
    }

    fun onFiltroSeleccionado(filtro: FiltroCompromiso) {
        _uiState.update { it.copy(filtro = filtro) }
        cargarLista(0)
    }

    fun onMesSeleccionado(mes: YearMonth?) {
        _uiState.update { it.copy(mes = mes) }
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
            val resultado = repositorio.listarDeEmpresa(
                sesion.empresaId(), estado.filtro.valorApi, estado.mes, estado.buscar, pagina.coerceAtLeast(0)
            )
            _uiState.update { it.copy(compromisos = resultado, cargando = false, error = null) }
        }
    }
}
