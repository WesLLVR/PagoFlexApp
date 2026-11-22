package cl.duocuc.pagoflex.data.remote

import cl.duocuc.pagoflex.model.Pagina
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

data class ErrorResponse(
    val message: String?
)

data class PaginaResponse<T>(
    val contenido: List<T>,
    val pagina: Int,
    val totalPaginas: Int,
    val totalElementos: Long
)

fun <T, R> PaginaResponse<T>.aPagina(convertir: (T) -> R) = Pagina(
    contenido = contenido.map(convertir),
    pagina = pagina,
    totalPaginas = totalPaginas,
    totalElementos = totalElementos
)

suspend fun <T> llamar(peticion: suspend () -> T): T = try {
    peticion()
} catch (e: HttpException) {
    throw Exception(mensajeDelServidor(e))
} catch (e: IOException) {
    throw Exception("No se pudo conectar con el servidor")
}

suspend fun <T> llamarONulo(peticion: suspend () -> T): T? = try {
    peticion()
} catch (e: HttpException) {
    if (e.code() == 404) null else throw Exception(mensajeDelServidor(e))
} catch (e: IOException) {
    throw Exception("No se pudo conectar con el servidor")
}

private fun mensajeDelServidor(e: HttpException): String {
    val cuerpo = e.response()?.errorBody()?.string()
    val mensaje = try {
        Gson().fromJson(cuerpo, ErrorResponse::class.java)?.message
    } catch (ex: Exception) {
        null
    }
    return if (mensaje.isNullOrBlank()) "Error del servidor (${e.code()})" else mensaje
}
