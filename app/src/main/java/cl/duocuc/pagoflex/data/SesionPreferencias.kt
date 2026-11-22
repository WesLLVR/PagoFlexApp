package cl.duocuc.pagoflex.data

import android.content.Context
import androidx.core.content.edit
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID = "id_usuario"
        private const val CLAVE_NOMBRE = "nombre"
        private const val CLAVE_ROL = "rol"
        private const val CLAVE_EMPRESA = "empresa_id"
        private const val SIN_VALOR = -1L
    }

    fun guardar(usuario: Usuario) {
        prefs.edit {
            putLong(CLAVE_ID, usuario.id)
            putString(CLAVE_NOMBRE, usuario.nombre)
            putString(CLAVE_ROL, usuario.rol.name)
            putLong(CLAVE_EMPRESA, usuario.empresaId ?: SIN_VALOR)
        }
    }

    fun idUsuario(): Long = prefs.getLong(CLAVE_ID, SIN_VALOR)

    fun nombre(): String = prefs.getString(CLAVE_NOMBRE, "") ?: ""

    fun rol(): Rol? = prefs.getString(CLAVE_ROL, null)?.let { Rol.valueOf(it) }

    fun empresaId(): Long = prefs.getLong(CLAVE_EMPRESA, SIN_VALOR)

    fun haySesion(): Boolean = idUsuario() != SIN_VALOR

    fun borrar() {
        prefs.edit { clear() }
    }
}
