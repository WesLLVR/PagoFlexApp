package cl.duocuc.pagoflex.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PagoApi {

    @POST("api/pagos")
    suspend fun pagar(@Body datos: PagoRequest): PagoRealizadoResponse

    @GET("api/pagos")
    suspend fun listar(
        @Query("usuarioId") usuarioId: Long,
        @Query("empresa") empresa: String?,
        @Query("desde") desde: String?,
        @Query("hasta") hasta: String?,
        @Query("buscar") buscar: String?,
        @Query("pagina") pagina: Int,
        @Query("tamano") tamano: Int
    ): PaginaResponse<PagoResponse>

    @GET("api/pagos/empresas")
    suspend fun empresas(@Query("usuarioId") usuarioId: Long): List<String>

    @GET("api/pagos/cumplimiento")
    suspend fun cumplimiento(@Query("usuarioId") usuarioId: Long): CumplimientoResponse

    @GET("api/pagos/compromiso/{compromisoId}")
    suspend fun porCompromiso(@Path("compromisoId") compromisoId: Long): PagoResponse
}
