package cl.duocuc.pagoflex.data.repository

import cl.duocuc.pagoflex.data.remote.ActualizarPerfilRequest
import cl.duocuc.pagoflex.data.remote.CambiarContrasenaRequest
import cl.duocuc.pagoflex.data.remote.LoginRequest
import cl.duocuc.pagoflex.data.remote.RecuperarRequest
import cl.duocuc.pagoflex.data.remote.RecuperarResponse
import cl.duocuc.pagoflex.data.remote.RestablecerRequest
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.remote.UsuarioApi
import cl.duocuc.pagoflex.data.remote.aUsuario
import cl.duocuc.pagoflex.data.remote.llamar
import cl.duocuc.pagoflex.data.remote.llamarONulo
import cl.duocuc.pagoflex.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UsuarioRepository(
    private val api: UsuarioApi = RetrofitClient.crear(UsuarioApi::class.java)
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario =
        llamar { api.iniciarSesion(LoginRequest(correo.trim(), contrasena)) }.aUsuario()

    suspend fun recuperar(correo: String): RecuperarResponse =
        llamar { api.recuperar(RecuperarRequest(correo.trim())) }

    suspend fun restablecer(correo: String, codigo: String, nueva: String) =
        llamar { api.restablecer(RestablecerRequest(correo.trim(), codigo.trim(), nueva)) }

    suspend fun obtener(id: Long): Usuario =
        llamar { api.obtener(id) }.aUsuario()

    suspend fun buscarPorRut(rut: String): Usuario? =
        llamarONulo { api.buscarPorRut(rut) }?.aUsuario()

    suspend fun actualizarPerfil(id: Long, nombre: String, correo: String, telefono: String, comuna: String): Usuario {
        val datos = ActualizarPerfilRequest(
            nombre = nombre.trim(),
            correo = correo.trim(),
            telefono = telefono.trim().ifBlank { null },
            comuna = comuna.trim().ifBlank { null }
        )
        return llamar { api.actualizarPerfil(id, datos) }.aUsuario()
    }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String) =
        llamar { api.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun subirAvatar(id: Long, bytes: ByteArray): Usuario {
        val cuerpo = bytes.toRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", cuerpo)
        return llamar { api.subirAvatar(id, parte) }.aUsuario()
    }

    suspend fun eliminarAvatar(id: Long): Usuario =
        llamar { api.eliminarAvatar(id) }.aUsuario()
}
