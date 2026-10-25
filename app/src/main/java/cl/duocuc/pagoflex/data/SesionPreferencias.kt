package cl.duocuc.pagoflex.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val CLAVE_ULTIMA_ACTUALIZACION = "ultima_actualizacion"
        private const val SIN_SESION = -1L
    }

    fun guardar(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }

    fun guardarUltimaActualizacion(fechaHora: String) {
        prefs.edit { putString(CLAVE_ULTIMA_ACTUALIZACION, fechaHora) }
    }

    fun leerUltimaActualizacion(): String? = prefs.getString(CLAVE_ULTIMA_ACTUALIZACION, null)
}
