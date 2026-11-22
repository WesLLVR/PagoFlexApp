package cl.duocuc.pagoflex.data.repository

import cl.duocuc.pagoflex.data.remote.CerrarRequest
import cl.duocuc.pagoflex.data.remote.CrearReporteRequest
import cl.duocuc.pagoflex.data.remote.ReabrirRequest
import cl.duocuc.pagoflex.data.remote.ReporteApi
import cl.duocuc.pagoflex.data.remote.ResolverRequest
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.remote.TomarRequest
import cl.duocuc.pagoflex.data.remote.aPagina
import cl.duocuc.pagoflex.data.remote.aReporte
import cl.duocuc.pagoflex.data.remote.aResumenReportes
import cl.duocuc.pagoflex.data.remote.llamar
import cl.duocuc.pagoflex.data.remote.llamarONulo
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.ResumenReportes
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReporteRepository(
    private val api: ReporteApi = RetrofitClient.crear(ReporteApi::class.java)
) {

    suspend fun crear(compromisoId: Long, usuarioId: Long, motivo: MotivoReporte, comentario: String): Reporte =
        llamar { api.crear(CrearReporteRequest(compromisoId, usuarioId, motivo.name, comentario.trim())) }.aReporte()

    suspend fun listar(
        estado: EstadoReporte?,
        usuarioId: Long?,
        agenteId: Long?,
        buscar: String,
        soloAtrasados: Boolean,
        pagina: Int,
        tamano: Int = 5
    ): Pagina<Reporte> = llamar {
        api.listar(estado?.name, usuarioId, agenteId, buscar.trim().ifBlank { null }, soloAtrasados, pagina, tamano)
    }.aPagina { it.aReporte() }

    suspend fun resumen(): ResumenReportes =
        llamar { api.resumen() }.aResumenReportes()

    suspend fun obtener(id: Long): Reporte =
        llamar { api.obtener(id) }.aReporte()

    suspend fun ultimoDeCompromiso(compromisoId: Long): Reporte? =
        llamarONulo { api.ultimoDeCompromiso(compromisoId) }?.aReporte()

    suspend fun tomar(id: Long, agenteId: Long): Reporte =
        llamar { api.tomar(id, TomarRequest(agenteId)) }.aReporte()

    suspend fun resolver(id: Long, agenteId: Long, respuesta: String): Reporte =
        llamar { api.resolver(id, ResolverRequest(agenteId, respuesta.trim())) }.aReporte()

    suspend fun reabrir(id: Long, usuarioId: Long, comentario: String): Reporte =
        llamar { api.reabrir(id, ReabrirRequest(usuarioId, comentario.trim())) }.aReporte()

    suspend fun cerrar(id: Long, usuarioId: Long): Reporte =
        llamar { api.cerrar(id, CerrarRequest(usuarioId)) }.aReporte()

    suspend fun subirFoto(id: Long, bytes: ByteArray): Reporte {
        val cuerpo = bytes.toRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "comprobante.jpg", cuerpo)
        return llamar { api.subirFoto(id, parte) }.aReporte()
    }
}
