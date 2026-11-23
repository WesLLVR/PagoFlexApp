package cl.duocuc.pagoflex.viewmodel.ejecutivo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.model.ResumenCobranza
import cl.duocuc.pagoflex.util.Fechas
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth

data class ResumenCobranzaUiState(
    val mes: YearMonth = YearMonth.from(Fechas.hoy()),
    val resumen: ResumenCobranza = ResumenCobranza(mes = YearMonth.from(Fechas.hoy())),
    val cargando: Boolean = true,
    val error: String? = null
)

class ResumenCobranzaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = CompromisoRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(ResumenCobranzaUiState())
    val uiState: StateFlow<ResumenCobranzaUiState> = _uiState.asStateFlow()

    fun recargar() {
        val mes = _uiState.value.mes
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val resumen = repositorio.resumenCobranza(sesion.empresaId(), mes)
            _uiState.update { if (it.mes == mes) it.copy(resumen = resumen, cargando = false, error = null) else it }
        }
    }

    fun onMesAnterior() {
        _uiState.update { it.copy(mes = it.mes.minusMonths(1)) }
        recargar()
    }

    fun onMesSiguiente() {
        _uiState.update { it.copy(mes = it.mes.plusMonths(1)) }
        recargar()
    }
}
