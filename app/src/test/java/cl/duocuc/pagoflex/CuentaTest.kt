package cl.duocuc.pagoflex

import cl.duocuc.pagoflex.data.remote.ActualizarPerfilRequest
import cl.duocuc.pagoflex.data.remote.CambiarContrasenaRequest
import cl.duocuc.pagoflex.data.remote.LoginRequest
import cl.duocuc.pagoflex.data.remote.RecuperarRequest
import cl.duocuc.pagoflex.data.remote.RecuperarResponse
import cl.duocuc.pagoflex.data.remote.RestablecerRequest
import cl.duocuc.pagoflex.data.remote.UsuarioApi
import cl.duocuc.pagoflex.data.remote.UsuarioResponse
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CuentaTest {

    private lateinit var apiFalsa: FakeUsuarioApi
    private lateinit var repositorio: UsuarioRepository

    @Before
    fun preparar() {
        apiFalsa = FakeUsuarioApi()
        repositorio = UsuarioRepository(api = apiFalsa)
    }

    @Test
    fun iniciarSesion_convierteLaRespuestaEnUsuario() = runBlocking {
        val usuario = repositorio.iniciarSesion("  camila@pagoflex.cl ", "123456")

        assertEquals(1L, usuario.id)
        assertEquals(Rol.USUARIO_FINAL, usuario.rol)
        assertEquals("Camila", usuario.primerNombre)
        assertEquals("camila@pagoflex.cl", apiFalsa.ultimoCorreo)
    }

    @Test
    fun iniciarSesion_conClaveIncorrecta_muestraElMensajeDelServidor() {
        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.iniciarSesion("camila@pagoflex.cl", "000000") }
        }

        assertEquals("Correo o contraseña incorrectos", error.message)
    }

    @Test
    fun buscarPorRut_inexistente_devuelveNull() = runBlocking {
        assertNull(repositorio.buscarPorRut("11.111.111-1"))
        assertEquals("Camila Andrea Rojas Peña", repositorio.buscarPorRut("15.834.207-3")?.nombre)
    }

    @Test
    fun sinConexion_lanzaErrorConMensajeAmigable() {
        apiFalsa.sinConexion = true

        val error = assertThrows(Exception::class.java) {
            runBlocking { repositorio.iniciarSesion("camila@pagoflex.cl", "123456") }
        }

        assertEquals("No se pudo conectar con el servidor", error.message)
    }

    @Test
    fun contrasenaNueva_exigeEntre6y20CaracteresSinEspacios() {
        assertNotNull(Validador.contrasenaNueva("123"))
        assertNotNull(Validador.contrasenaNueva("clave con espacio"))
        assertNull(Validador.contrasenaNueva("pagoflex1"))
    }

    @Test
    fun confirmacion_debeCoincidir() {
        assertEquals("Las contraseñas no coinciden", Validador.confirmacion("pagoflex1", "pagoflex2"))
        assertNull(Validador.confirmacion("pagoflex1", "pagoflex1"))
    }

    @Test
    fun codigo_debeTenerSeisNumeros() {
        assertNotNull(Validador.codigo("12345"))
        assertNotNull(Validador.codigo("12a456"))
        assertNull(Validador.codigo("123456"))
    }

    @Test
    fun urlServidor_aceptaIpOTunelYLaNormaliza() {
        assertNull(Validador.urlServidor("192.168.1.15:8080"))
        assertEquals("http://192.168.1.15:8080/", Validador.normalizarUrl("192.168.1.15:8080"))
        assertEquals("https://pagoflex-8080.brs.devtunnels.ms/", Validador.normalizarUrl(" https://pagoflex-8080.brs.devtunnels.ms "))
        assertNotNull(Validador.urlServidor(""))
        assertNotNull(Validador.urlServidor("http://mi servidor"))
    }
}

private fun errorDelServidor(codigo: Int, mensaje: String): HttpException =
    HttpException(
        Response.error<Any>(codigo, """{"message":"$mensaje"}""".toResponseBody("application/json".toMediaType()))
    )

private val camila = UsuarioResponse(
    id = 1,
    nombre = "Camila Andrea Rojas Peña",
    rut = "15.834.207-3",
    correo = "camila@pagoflex.cl",
    telefono = "+56 9 0000 0101",
    comuna = "Maipú",
    rol = "USUARIO_FINAL",
    rolNombre = "Usuario final",
    empresaId = null,
    cargo = null,
    avatarUrl = null
)

private class FakeUsuarioApi : UsuarioApi {

    var sinConexion = false
    var ultimoCorreo: String? = null

    override suspend fun iniciarSesion(datos: LoginRequest): UsuarioResponse {
        if (sinConexion) throw IOException("Servidor apagado")
        ultimoCorreo = datos.correo
        if (datos.contrasena != "123456") throw errorDelServidor(401, "Correo o contraseña incorrectos")
        return camila
    }

    override suspend fun buscarPorRut(rut: String): UsuarioResponse {
        if (rut == camila.rut) return camila
        throw errorDelServidor(404, "No existe una persona con ese RUT")
    }

    override suspend fun recuperar(datos: RecuperarRequest) = RecuperarResponse(datos.correo, "123456", 15)
    override suspend fun restablecer(datos: RestablecerRequest) = Unit
    override suspend fun obtener(id: Long): UsuarioResponse = camila
    override suspend fun actualizarPerfil(id: Long, datos: ActualizarPerfilRequest): UsuarioResponse = TODO()
    override suspend fun cambiarContrasena(id: Long, datos: CambiarContrasenaRequest) = TODO()
    override suspend fun subirAvatar(id: Long, archivo: MultipartBody.Part): UsuarioResponse = TODO()
    override suspend fun eliminarAvatar(id: Long): UsuarioResponse = TODO()
}
