package cl.duocuc.pagoflex.data

import android.content.Context
import androidx.core.content.edit
import cl.duocuc.pagoflex.model.ConfiguracionAvisos

class AvisosPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("avisos", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ACTIVOS = "activos"
        private const val CLAVE_DIAS = "dias_anticipacion"
        private const val DIAS_POR_DEFECTO = 3
    }

    fun guardar(configuracion: ConfiguracionAvisos) {
        prefs.edit {
            putBoolean(CLAVE_ACTIVOS, configuracion.activos)
            putInt(CLAVE_DIAS, configuracion.diasAnticipacion)
        }
    }

    fun leer(): ConfiguracionAvisos = ConfiguracionAvisos(
        activos = prefs.getBoolean(CLAVE_ACTIVOS, true),
        diasAnticipacion = prefs.getInt(CLAVE_DIAS, DIAS_POR_DEFECTO)
    )
}
