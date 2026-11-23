package cl.duocuc.pagoflex.viewmodel.ejecutivo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EjecutivoDetalleUiState(
    val compromiso: Compromiso? = null,
    val reporte: Reporte? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val mostrarAnular: Boolean = false,
    val motivoAnulacion: MotivoAnulacion? = null,
    val comentarioAnulacion: String = "",
    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val eliminado: Boolean = false
)

class EjecutivoDetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val reportes = ReporteRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(EjecutivoDetalleUiState())
    val uiState: StateFlow<EjecutivoDetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: Long) {
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val compromiso = compromisos.obtener(id)
            val reporte = reportes.ultimoDeCompromiso(id)
            _uiState.update { it.copy(compromiso = compromiso, reporte = reporte, cargando = false, error = null) }
        }
    }

    fun onMostrarAnular(mostrar: Boolean) = _uiState.update {
        it.copy(
            mostrarAnular = mostrar,
            motivoAnulacion = null,
            comentarioAnulacion = "",
            errorMotivo = null,
            errorComentario = null
        )
    }

    fun onMotivoAnulacion(motivo: MotivoAnulacion) = _uiState.update { it.copy(motivoAnulacion = motivo, errorMotivo = null) }

    fun onComentarioAnulacion(texto: String) = _uiState.update { it.copy(comentarioAnulacion = texto, errorComentario = null) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onAnular() {
        val estado = _uiState.value
        val compromiso = estado.compromiso ?: return
        val errorMotivo = Validador.motivoAnulacion(estado.motivoAnulacion)
        val errorComentario = Validador.comentarioAnulacion(estado.motivoAnulacion, estado.comentarioAnulacion)
        _uiState.update { it.copy(errorMotivo = errorMotivo, errorComentario = errorComentario) }
        val motivo = estado.motivoAnulacion
        if (motivo == null || errorComentario != null) return

        _uiState.update { it.copy(procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            val anulado = compromisos.anular(compromiso.id, motivo, estado.comentarioAnulacion, sesion.idUsuario())
            _uiState.update {
                it.copy(compromiso = anulado, procesando = false, mostrarAnular = false, mensaje = "Compromiso anulado")
            }
        }
    }

    fun onEliminar() {
        val compromiso = _uiState.value.compromiso ?: return
        _uiState.update { it.copy(procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            compromisos.eliminar(compromiso.id)
            _uiState.update { it.copy(procesando = false, eliminado = true) }
        }
    }
}
