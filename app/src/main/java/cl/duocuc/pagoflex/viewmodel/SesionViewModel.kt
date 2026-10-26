package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val repository = UsuarioRepository(db)
    private val sesion = SesionPreferencias(application)

    val sesiones: List<Usuario> = repository.sesionesDisponibles()

    private val _usuarioActual = MutableStateFlow(repository.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        sesion.guardar(usuario.id)
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        sesion.borrar()
        _usuarioActual.value = null
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
