package cl.duocuc.pagoflex.viewmodel.cuenta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.notificaciones.Notificaciones
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecuperarUiState(
    val correo: String = "",
    val codigo: String = "",
    val nueva: String = "",
    val repetida: String = "",
    val codigoEnviado: Boolean = false,
    val codigoEnPantalla: String? = null,
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val errorNueva: String? = null,
    val errorRepetida: String? = null,
    val error: String? = null,
    val cargando: Boolean = false,
    val restablecida: Boolean = false
)

class RecuperarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()

    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(texto: String) = _uiState.update { it.copy(correo = texto, errorCorreo = null, error = null) }

    fun onCodigoCambia(texto: String) =
        _uiState.update { it.copy(codigo = texto.filter { c -> c.isDigit() }.take(6), errorCodigo = null, error = null) }

    fun onNuevaCambia(texto: String) = _uiState.update { it.copy(nueva = texto, errorNueva = null) }

    fun onRepetidaCambia(texto: String) = _uiState.update { it.copy(repetida = texto, errorRepetida = null) }

    fun onSolicitarCodigo() {
        val errorCorreo = Validador.correo(_uiState.value.correo)
        _uiState.update { it.copy(errorCorreo = errorCorreo) }
        if (errorCorreo != null) return

        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val respuesta = repositorio.recuperar(_uiState.value.correo)
            val app = getApplication<Application>()
            val notificado = Notificaciones.mostrar(
                context = app,
                id = Notificaciones.ID_CODIGO,
                canal = Notificaciones.CANAL_CUENTA,
                titulo = app.getString(R.string.notificacion_codigo_titulo),
                texto = app.getString(R.string.notificacion_codigo_texto, respuesta.codigo, respuesta.minutosVigencia)
            )
            _uiState.update {
                it.copy(
                    cargando = false,
                    codigoEnviado = true,
                    codigoEnPantalla = if (notificado) null else respuesta.codigo
                )
            }
        }
    }

    fun onRestablecer() {
        val estado = _uiState.value
        val errorCodigo = Validador.codigo(estado.codigo)
        val errorNueva = Validador.contrasenaNueva(estado.nueva)
        val errorRepetida = Validador.confirmacion(estado.nueva, estado.repetida)
        _uiState.update { it.copy(errorCodigo = errorCodigo, errorNueva = errorNueva, errorRepetida = errorRepetida) }
        if (errorCodigo != null || errorNueva != null || errorRepetida != null) return

        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            repositorio.restablecer(estado.correo, estado.codigo, estado.nueva)
            _uiState.update { it.copy(cargando = false, restablecida = true) }
        }
    }

    fun onCambiarCorreo() = _uiState.update { RecuperarUiState(correo = it.correo) }
}
