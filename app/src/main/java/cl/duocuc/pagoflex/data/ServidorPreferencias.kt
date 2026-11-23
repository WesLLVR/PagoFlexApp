package cl.duocuc.pagoflex.data

import android.content.Context
import androidx.core.content.edit
import cl.duocuc.pagoflex.data.remote.RetrofitClient

class ServidorPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("servidor", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_URL = "url"
    }

    fun guardar(url: String) {
        prefs.edit { putString(CLAVE_URL, url) }
    }

    fun leer(): String = prefs.getString(CLAVE_URL, RetrofitClient.URL_POR_DEFECTO) ?: RetrofitClient.URL_POR_DEFECTO
}
