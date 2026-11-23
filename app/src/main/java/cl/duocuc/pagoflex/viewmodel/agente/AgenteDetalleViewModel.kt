package cl.duocuc.pagoflex.viewmodel.agente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AgenteDetalleUiState(
    val reporte: Reporte? = null,
    val idAgente: Long = 0,
    val cargando: Boolean = true,
    val error: String? = null,
    val respuesta: String = "",
    val errorRespuesta: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null
)

class AgenteDetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ReporteRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(AgenteDetalleUiState(idAgente = sesion.idUsuario()))
    val uiState: StateFlow<AgenteDetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: Long) {
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val reporte = repositorio.obtener(id)
            _uiState.update { it.copy(reporte = reporte, cargando = false, error = null) }
        }
    }

    fun onRespuestaCambia(texto: String) = _uiState.update { it.copy(respuesta = texto, errorRespuesta = null) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onTomar() {
        val reporte = _uiState.value.reporte ?: return
        trabajar("Tomaste el reporte ${reporte.numero}") {
            repositorio.tomar(reporte.id, sesion.idUsuario())
        }
    }

    fun onResolver() {
        val estado = _uiState.value
        val reporte = estado.reporte ?: return
        val error = Validador.respuestaAgente(estado.respuesta)
        _uiState.update { it.copy(errorRespuesta = error) }
        if (error != null) return
        trabajar("Reporte resuelto. El usuario ya puede ver tu respuesta") {
            val resuelto = repositorio.resolver(reporte.id, sesion.idUsuario(), estado.respuesta)
            _uiState.update { it.copy(respuesta = "") }
            resuelto
        }
    }

    private fun trabajar(mensajeExito: String, accion: suspend () -> Reporte) {
        _uiState.update { it.copy(procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            val actualizado = accion()
            _uiState.update { it.copy(reporte = actualizado, procesando = false, mensaje = mensajeExito) }
        }
    }
}
