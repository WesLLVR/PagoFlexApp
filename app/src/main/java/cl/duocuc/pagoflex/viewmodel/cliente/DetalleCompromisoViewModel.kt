package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DetalleCompromisoUiState(
    val compromiso: Compromiso? = null,
    val cuotas: List<Compromiso> = emptyList(),
    val motivoNoPagable: String? = null,
    val reporte: Reporte? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val mostrarReabrir: Boolean = false,
    val comentarioReabrir: String = "",
    val errorReabrir: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null
)

class DetalleCompromisoViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val reportes = ReporteRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(DetalleCompromisoUiState())
    val uiState: StateFlow<DetalleCompromisoUiState> = _uiState.asStateFlow()

    fun cargar(id: Long) {
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val compromiso = compromisos.obtener(id)
            val cuotas = compromisos.cuotas(id)
            val motivo = if (compromiso.sePuedePagar) compromisos.motivoNoPagable(id) else null
            val reporte = reportes.ultimoDeCompromiso(id)
            _uiState.update {
                it.copy(
                    compromiso = compromiso,
                    cuotas = cuotas,
                    motivoNoPagable = motivo,
                    reporte = reporte,
                    cargando = false,
                    error = null
                )
            }
        }
    }

    fun onMostrarReabrir(mostrar: Boolean) =
        _uiState.update { it.copy(mostrarReabrir = mostrar, comentarioReabrir = "", errorReabrir = null) }

    fun onComentarioReabrir(texto: String) = _uiState.update { it.copy(comentarioReabrir = texto, errorReabrir = null) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onCerrarReporte() {
        val reporte = _uiState.value.reporte ?: return
        trabajar("¡Listo! Cerraste el reporte ${reporte.numero}") {
            reportes.cerrar(reporte.id, sesion.idUsuario())
        }
    }

    fun onReabrirReporte() {
        val estado = _uiState.value
        val reporte = estado.reporte ?: return
        val error = Validador.comentarioReapertura(estado.comentarioReabrir)
        _uiState.update { it.copy(errorReabrir = error) }
        if (error != null) return
        trabajar("Reabrimos tu reporte. Un agente lo revisará otra vez") {
            reportes.reabrir(reporte.id, sesion.idUsuario(), estado.comentarioReabrir)
            _uiState.update { it.copy(mostrarReabrir = false, comentarioReabrir = "") }
        }
    }

    private fun trabajar(mensajeExito: String, accion: suspend () -> Unit) {
        val id = _uiState.value.compromiso?.id ?: return
        _uiState.update { it.copy(procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            accion()
            _uiState.update { it.copy(procesando = false, mensaje = mensajeExito) }
            cargar(id)
        }
    }
}
