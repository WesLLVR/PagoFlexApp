package cl.duocuc.pagoflex.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReporteApi {

    @POST("api/reportes")
    suspend fun crear(@Body datos: CrearReporteRequest): ReporteResponse

    @GET("api/reportes")
    suspend fun listar(
        @Query("estado") estado: String?,
        @Query("usuarioId") usuarioId: Long?,
        @Query("agenteId") agenteId: Long?,
        @Query("buscar") buscar: String?,
        @Query("soloAtrasados") soloAtrasados: Boolean,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int
    ): PaginaResponse<ReporteResponse>

    @GET("api/reportes/resumen")
    suspend fun resumen(): ResumenReportesResponse

    @GET("api/reportes/{id}")
    suspend fun obtener(@Path("id") id: Long): ReporteResponse

    @GET("api/reportes/compromiso/{compromisoId}")
    suspend fun ultimoDeCompromiso(@Path("compromisoId") compromisoId: Long): ReporteResponse

    @PATCH("api/reportes/{id}/tomar")
    suspend fun tomar(@Path("id") id: Long, @Body datos: TomarRequest): ReporteResponse

    @PATCH("api/reportes/{id}/resolver")
    suspend fun resolver(@Path("id") id: Long, @Body datos: ResolverRequest): ReporteResponse

    @PATCH("api/reportes/{id}/reabrir")
    suspend fun reabrir(@Path("id") id: Long, @Body datos: ReabrirRequest): ReporteResponse

    @PATCH("api/reportes/{id}/cerrar")
    suspend fun cerrar(@Path("id") id: Long, @Body datos: CerrarRequest): ReporteResponse

    @Multipart
    @POST("api/reportes/{id}/foto")
    suspend fun subirFoto(@Path("id") id: Long, @Part archivo: MultipartBody.Part): ReporteResponse
}
