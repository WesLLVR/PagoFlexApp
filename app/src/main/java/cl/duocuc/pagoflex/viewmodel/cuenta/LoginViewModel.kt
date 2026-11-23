package cl.duocuc.pagoflex.viewmodel.cuenta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.ServidorPreferencias
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.notificaciones.AvisosWorker
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val error: String? = null,
    val cargando: Boolean = false,
    val ingresado: Usuario? = null,
    val servidor: String = "",
    val mostrarServidor: Boolean = false,
    val textoServidor: String = "",
    val errorServidor: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()
    private val sesion = SesionPreferencias(application)
    private val servidorPreferencias = ServidorPreferencias(application)

    private val _uiState = MutableStateFlow(LoginUiState(servidor = servidorPreferencias.leer()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(texto: String) = _uiState.update { it.copy(correo = texto, errorCorreo = null, error = null) }

    fun onContrasenaCambia(texto: String) =
        _uiState.update { it.copy(contrasena = texto, errorContrasena = null, error = null) }

    fun onIngresar() {
        val estado = _uiState.value
        val errorCorreo = Validador.correo(estado.correo)
        val errorContrasena = Validador.contrasenaIngreso(estado.contrasena)
        _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
        if (errorCorreo != null || errorContrasena != null) return

        _uiState.update { it.copy(cargando = true, error = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, error = mensaje) } }) {
            val usuario = repositorio.iniciarSesion(estado.correo, estado.contrasena)
            sesion.guardar(usuario)
            if (usuario.rol == Rol.USUARIO_FINAL) {
                AvisosWorker.programar(getApplication())
                AvisosWorker.revisarAhora(getApplication())
            }
            _uiState.update { it.copy(cargando = false, ingresado = usuario, contrasena = "") }
        }
    }

    fun onNavegado() = _uiState.update { LoginUiState(correo = it.correo, servidor = it.servidor) }

    fun onMostrarServidor(mostrar: Boolean) =
        _uiState.update { it.copy(mostrarServidor = mostrar, textoServidor = it.servidor, errorServidor = null) }

    fun onServidorCambia(texto: String) = _uiState.update { it.copy(textoServidor = texto, errorServidor = null) }

    fun onRestablecerServidor() = _uiState.update { it.copy(textoServidor = RetrofitClient.URL_POR_DEFECTO, errorServidor = null) }

    fun onGuardarServidor() {
        val texto = _uiState.value.textoServidor
        val error = Validador.urlServidor(texto)
        if (error != null) {
            _uiState.update { it.copy(errorServidor = error) }
            return
        }
        val url = Validador.normalizarUrl(texto)
        servidorPreferencias.guardar(url)
        RetrofitClient.cambiarServidor(url)
        _uiState.update { it.copy(servidor = url, mostrarServidor = false, error = null) }
    }
}
