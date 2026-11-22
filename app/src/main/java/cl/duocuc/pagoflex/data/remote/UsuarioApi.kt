package cl.duocuc.pagoflex.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UsuarioApi {

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioResponse

    @POST("api/usuarios/recuperar")
    suspend fun recuperar(@Body datos: RecuperarRequest): RecuperarResponse

    @POST("api/usuarios/restablecer")
    suspend fun restablecer(@Body datos: RestablecerRequest)

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @GET("api/usuarios/rut/{rut}")
    suspend fun buscarPorRut(@Path("rut") rut: String): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Long, @Body datos: ActualizarPerfilRequest): UsuarioResponse

    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Long, @Body datos: CambiarContrasenaRequest)

    @Multipart
    @POST("api/usuarios/{id}/avatar")
    suspend fun subirAvatar(@Path("id") id: Long, @Part archivo: MultipartBody.Part): UsuarioResponse

    @DELETE("api/usuarios/{id}/avatar")
    suspend fun eliminarAvatar(@Path("id") id: Long): UsuarioResponse
}
