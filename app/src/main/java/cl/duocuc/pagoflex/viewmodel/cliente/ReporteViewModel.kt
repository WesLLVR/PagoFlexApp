package cl.duocuc.pagoflex.viewmodel.cliente

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.ConvertidorImagen
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReporteUiState(
    val compromiso: Compromiso? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val motivo: MotivoReporte? = null,
    val comentario: String = "",
    val foto: ByteArray? = null,
    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val errorFoto: String? = null,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val enviado: Boolean = false
)

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val reportes = ReporteRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    fun cargar(compromisoId: Long) {
        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val compromiso = compromisos.obtener(compromisoId)
            _uiState.update { it.copy(compromiso = compromiso, cargando = false) }
        }
    }

    fun onMotivoSeleccionado(motivo: MotivoReporte) = _uiState.update {
        it.copy(motivo = motivo, errorMotivo = null, errorComentario = Validador.comentarioReporte(motivo, it.comentario))
    }

    fun onComentarioCambia(texto: String) = _uiState.update {
        it.copy(comentario = texto, errorComentario = Validador.comentarioReporte(it.motivo, texto))
    }

    fun onFotoTomada(foto: Bitmap) = _uiState.update { it.copy(foto = ConvertidorImagen.aBytes(foto), errorFoto = null) }

    fun onImagenElegida(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri)
        _uiState.update {
            if (imagen == null) it.copy(errorFoto = "No pudimos leer esa imagen")
            else it.copy(foto = ConvertidorImagen.aBytes(imagen), errorFoto = null)
        }
    }

    fun onQuitarFoto() = _uiState.update { it.copy(foto = null) }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onEnviar() {
        val estado = _uiState.value
        val compromiso = estado.compromiso ?: return
        val errorMotivo = Validador.motivoReporte(estado.motivo)
        val errorComentario = Validador.comentarioReporte(estado.motivo, estado.comentario)
        _uiState.update { it.copy(errorMotivo = errorMotivo, errorComentario = errorComentario) }
        val motivo = estado.motivo
        if (motivo == null || errorComentario != null) return

        _uiState.update { it.copy(enviando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(enviando = false, mensaje = mensaje) } }) {
            val reporte = reportes.crear(compromiso.id, sesion.idUsuario(), motivo, estado.comentario)
            if (estado.foto != null) {
                try {
                    reportes.subirFoto(reporte.id, estado.foto)
                } catch (e: Exception) {
                    _uiState.update { it.copy(mensaje = "El reporte se envió, pero no pudimos subir la foto") }
                }
            }
            _uiState.update { it.copy(enviando = false, enviado = true) }
        }
    }
}
