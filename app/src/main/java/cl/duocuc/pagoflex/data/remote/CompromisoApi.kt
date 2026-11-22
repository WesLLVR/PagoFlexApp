package cl.duocuc.pagoflex.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CompromisoApi {

    @GET("api/compromisos")
    suspend fun listar(
        @Query("usuarioId") usuarioId: Long?,
        @Query("empresaId") empresaId: Long?,
        @Query("estado") estado: String?,
        @Query("buscar") buscar: String?,
        @Query("mes") mes: String?,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int
    ): PaginaResponse<CompromisoResponse>

    @GET("api/compromisos/{id}")
    suspend fun obtener(@Path("id") id: Long): CompromisoResponse

    @GET("api/compromisos/{id}/cuotas")
    suspend fun cuotas(@Path("id") id: Long): List<CompromisoResponse>

    @GET("api/compromisos/{id}/pagable")
    suspend fun pagable(@Path("id") id: Long): PagableResponse

    @GET("api/compromisos/resumen-mes")
    suspend fun resumenMes(@Query("usuarioId") usuarioId: Long): ResumenMesResponse

    @GET("api/compromisos/proximos")
    suspend fun proximos(@Query("usuarioId") usuarioId: Long, @Query("dias") dias: Int): List<CompromisoResponse>

    @GET("api/compromisos/resumen-cobranza")
    suspend fun resumenCobranza(@Query("empresaId") empresaId: Long, @Query("mes") mes: String): ResumenCobranzaResponse

    @POST("api/compromisos")
    suspend fun registrar(@Body datos: RegistroCompromisoRequest): CompromisoResponse

    @PATCH("api/compromisos/{id}/anular")
    suspend fun anular(@Path("id") id: Long, @Body datos: AnularRequest): CompromisoResponse

    @DELETE("api/compromisos/{id}")
    suspend fun eliminar(@Path("id") id: Long)

    @GET("api/empresas/{id}")
    suspend fun empresa(@Path("id") id: Long): EmpresaResponse
}
