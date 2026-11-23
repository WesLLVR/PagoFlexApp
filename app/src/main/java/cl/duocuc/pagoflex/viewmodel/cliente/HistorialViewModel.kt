package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class HistorialUiState(
    val empresas: List<String> = emptyList(),
    val empresa: String? = null,
    val desde: LocalDate? = null,
    val hasta: LocalDate? = null,
    val errorFechas: String? = null,
    val buscar: String = "",
    val pagos: Pagina<Pago> = Pagina(),
    val cargando: Boolean = true,
    val error: String? = null
)

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = PagoRepository()
    private val sesion = SesionPreferencias(application)
    private var trabajoLista: Job? = null

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    init {
        ejecutar(onError = {}) {
            val empresas = repositorio.empresas(sesion.idUsuario())
            _uiState.update { it.copy(empresas = empresas) }
        }
        cargar(0)
    }

    fun onEmpresaSeleccionada(empresa: String?) {
        _uiState.update { it.copy(empresa = empresa) }
        cargar(0)
    }

    fun onDesdeElegida(fecha: LocalDate?) {
        _uiState.update { it.copy(desde = fecha) }
        cargar(0)
    }

    fun onHastaElegida(fecha: LocalDate?) {
        _uiState.update { it.copy(hasta = fecha) }
        cargar(0)
    }

    fun onBuscarCambia(texto: String) {
        _uiState.update { it.copy(buscar = texto) }
        cargar(0)
    }

    fun onPaginaAnterior() = cargar(_uiState.value.pagos.pagina - 1)

    fun onPaginaSiguiente() = cargar(_uiState.value.pagos.pagina + 1)

    fun recargar() = cargar(_uiState.value.pagos.pagina)

    private fun cargar(pagina: Int) {
        val estado = _uiState.value
        val errorFechas = Validador.rangoFechas(estado.desde, estado.hasta)
        _uiState.update { it.copy(errorFechas = errorFechas) }
        if (errorFechas != null) return

        trabajoLista?.cancel()
        trabajoLista = ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resultado = repositorio.listar(
                usuarioId = sesion.idUsuario(),
                empresa = estado.empresa,
                desde = estado.desde,
                hasta = estado.hasta,
                buscar = estado.buscar,
                pagina = pagina.coerceAtLeast(0)
            )
            _uiState.update { it.copy(pagos = resultado, cargando = false, error = null) }
        }
    }
}
