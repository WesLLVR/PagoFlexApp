package cl.duocuc.pagoflex.viewmodel.cuenta

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.ConvertidorImagen
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilUiState(
    val usuario: Usuario? = null,
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val comuna: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val actual: String = "",
    val nueva: String = "",
    val repetida: String = "",
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetida: String? = null,
    val procesando: Boolean = false,
    val mensaje: String? = null
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = UsuarioRepository()
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(cargando = false, errorCarga = mensaje) } }) {
            mostrarUsuario(repositorio.obtener(sesion.idUsuario()))
            _uiState.update { it.copy(cargando = false) }
        }
    }

    fun onNombreCambia(texto: String) = _uiState.update { it.copy(nombre = texto, errorNombre = null) }
    fun onCorreoCambia(texto: String) = _uiState.update { it.copy(correo = texto, errorCorreo = null) }
    fun onTelefonoCambia(texto: String) = _uiState.update { it.copy(telefono = texto, errorTelefono = null) }
    fun onComunaCambia(texto: String) = _uiState.update { it.copy(comuna = texto) }
    fun onActualCambia(texto: String) = _uiState.update { it.copy(actual = texto, errorActual = null) }
    fun onNuevaCambia(texto: String) = _uiState.update { it.copy(nueva = texto, errorNueva = null) }
    fun onRepetidaCambia(texto: String) = _uiState.update { it.copy(repetida = texto, errorRepetida = null) }
    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onGuardarDatos() {
        val estado = _uiState.value
        val errorNombre = Validador.nombre(estado.nombre)
        val errorCorreo = Validador.correo(estado.correo)
        val errorTelefono = if (estado.telefono.isBlank()) null else Validador.telefono(estado.telefono)
        _uiState.update { it.copy(errorNombre = errorNombre, errorCorreo = errorCorreo, errorTelefono = errorTelefono) }
        if (errorNombre != null || errorCorreo != null || errorTelefono != null) return

        trabajar("Tus datos quedaron guardados") {
            val actualizado = repositorio.actualizarPerfil(
                sesion.idUsuario(), estado.nombre, estado.correo, estado.telefono, estado.comuna
            )
            sesion.guardar(actualizado)
            mostrarUsuario(actualizado)
        }
    }

    fun onCambiarContrasena() {
        val estado = _uiState.value
        val errorActual = Validador.contrasenaIngreso(estado.actual)
        val errorNueva = Validador.contrasenaNueva(estado.nueva)
        val errorRepetida = Validador.confirmacion(estado.nueva, estado.repetida)
        _uiState.update { it.copy(errorActual = errorActual, errorNueva = errorNueva, errorRepetida = errorRepetida) }
        if (errorActual != null || errorNueva != null || errorRepetida != null) return

        trabajar("Tu contraseña fue cambiada") {
            repositorio.cambiarContrasena(sesion.idUsuario(), estado.actual, estado.nueva)
            _uiState.update { it.copy(actual = "", nueva = "", repetida = "") }
        }
    }

    fun onFotoTomada(foto: Bitmap) = subirFoto(ConvertidorImagen.aBytes(foto))

    fun onImagenElegida(uri: Uri) {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri)
        if (imagen == null) {
            _uiState.update { it.copy(mensaje = "No pudimos leer esa imagen") }
            return
        }
        subirFoto(ConvertidorImagen.aBytes(imagen))
    }

    fun onQuitarFoto() = trabajar("Quitamos tu foto de perfil") {
        mostrarUsuario(repositorio.eliminarAvatar(sesion.idUsuario()))
    }

    private fun subirFoto(bytes: ByteArray) = trabajar("Tu foto de perfil fue actualizada") {
        mostrarUsuario(repositorio.subirAvatar(sesion.idUsuario(), bytes))
    }

    private fun trabajar(mensajeExito: String, accion: suspend () -> Unit) {
        _uiState.update { it.copy(procesando = true) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, mensaje = mensaje) } }) {
            accion()
            _uiState.update { it.copy(procesando = false, mensaje = mensajeExito) }
        }
    }

    private fun mostrarUsuario(usuario: Usuario) = _uiState.update {
        it.copy(
            usuario = usuario,
            nombre = usuario.nombre,
            correo = usuario.correo,
            telefono = usuario.telefono ?: "",
            comuna = usuario.comuna ?: ""
        )
    }
}
