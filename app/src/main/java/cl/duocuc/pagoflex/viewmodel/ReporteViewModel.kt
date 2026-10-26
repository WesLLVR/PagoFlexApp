package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.ConvertidorImagen
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.MotivoReporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReporteUiState(
    val motivo: MotivoReporte? = null,
    val comentario: String = "",
    val foto: ByteArray? = null,
    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val errorFoto: String? = null,
    val enviado: Boolean = false
)

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val repository = ReporteRepository(db)

    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    fun onMotivoSeleccionado(motivo: MotivoReporte) {
        _uiState.update { it.copy(motivo = motivo, errorMotivo = null, errorComentario = null) }
    }

    fun onComentarioCambia(valor: String) {
        _uiState.update { it.copy(comentario = valor, errorComentario = null) }
    }

    fun onFotoTomada(foto: Bitmap) {
        _uiState.update { it.copy(foto = ConvertidorImagen.aBytes(foto), errorFoto = null) }
    }

    fun onImagenElegida(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri)
        if (imagen == null) {
            _uiState.update { it.copy(errorFoto = "No pudimos leer esa imagen. Prueba con otra.") }
            return
        }
        _uiState.update { it.copy(foto = ConvertidorImagen.aBytes(imagen), errorFoto = null) }
    }

    fun onQuitarFoto() {
        _uiState.update { it.copy(foto = null) }
    }

    fun onEnviar(idCompromiso: Long): Boolean {
        val estado = _uiState.value
        val errorMotivo = Validador.motivoReporte(estado.motivo)
        val errorComentario = Validador.comentarioReporte(estado.motivo, estado.comentario)
        _uiState.update { it.copy(errorMotivo = errorMotivo, errorComentario = errorComentario) }
        val motivo = estado.motivo
        if (errorMotivo != null || errorComentario != null || motivo == null) return false
        repository.crear(idCompromiso, motivo, estado.comentario, estado.foto)
        _uiState.update { it.copy(enviado = true) }
        return true
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
