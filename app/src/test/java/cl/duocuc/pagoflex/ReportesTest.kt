package cl.duocuc.pagoflex

import cl.duocuc.pagoflex.data.remote.CerrarRequest
import cl.duocuc.pagoflex.data.remote.CrearReporteRequest
import cl.duocuc.pagoflex.data.remote.HistorialResponse
import cl.duocuc.pagoflex.data.remote.PaginaResponse
import cl.duocuc.pagoflex.data.remote.ReabrirRequest
import cl.duocuc.pagoflex.data.remote.ReporteApi
import cl.duocuc.pagoflex.data.remote.ReporteResponse
import cl.duocuc.pagoflex.data.remote.ResolverRequest
import cl.duocuc.pagoflex.data.remote.ResumenReportesResponse
import cl.duocuc.pagoflex.data.remote.TomarRequest
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ReportesTest {

    private lateinit var apiFalsa: FakeReporteApi
    private lateinit var repositorio: ReporteRepository

    @Before
    fun preparar() {
        apiFalsa = FakeReporteApi()
        repositorio = ReporteRepository(api = apiFalsa)
    }

    @Test
    fun crear_enviaElMotivoComoTextoYConvierteElEstado() = runBlocking {
        val reporte = repositorio.crear(30, 1, MotivoReporte.NO_RECONOZCO, "No reconozco el cobro")

        assertEquals("NO_RECONOZCO", apiFalsa.ultimoMotivo)
        assertEquals(EstadoReporte.ABIERTO, reporte.estado)
        assertEquals(MotivoReporte.NO_RECONOZCO, reporte.motivo)
    }

    @Test
    fun listar_sinFiltroEnviaNullYConAgenteLoEnvia() = runBlocking {
        repositorio.listar(null, null, 9, "", false, 0)

        assertNull(apiFalsa.ultimoEstado)
        assertEquals(9L, apiFalsa.ultimoAgente)

        repositorio.listar(EstadoReporte.EN_GESTION, null, null, "", false, 0)

        assertEquals("EN_GESTION", apiFalsa.ultimoEstado)
    }

    @Test
    fun tomar_devuelveElHistorialEnOrden() = runBlocking {
        val reporte = repositorio.tomar(13, 9)

        assertEquals(EstadoReporte.EN_GESTION, reporte.estado)
        assertEquals(listOf(EstadoReporte.ABIERTO, EstadoReporte.EN_GESTION), reporte.historial.map { it.estado })
    }

    @Test
    fun ultimoDeCompromiso_sinReportes_devuelveNull() = runBlocking {
        assertNull(repositorio.ultimoDeCompromiso(99))
        assertEquals("REP-0013", repositorio.ultimoDeCompromiso(30)?.numero)
    }

    @Test
    fun motivoOtroExigeComentario() {
        assertNotNull(Validador.comentarioReporte(MotivoReporte.OTRO, ""))
        assertNull(Validador.comentarioReporte(MotivoReporte.YA_PAGUE, ""))
    }

    @Test
    fun comentarioReapertura_exigeExplicarQueFalta() {
        assertNotNull(Validador.comentarioReapertura("No"))
        assertNull(Validador.comentarioReapertura("Sigue apareciendo como pendiente"))
    }
}

private class FakeReporteApi : ReporteApi {

    var ultimoMotivo: String? = null
    var ultimoEstado: String? = "sin llamar"
    var ultimoAgente: Long? = null

    private fun reporte(id: Long, estado: String, historial: List<HistorialResponse>) = ReporteResponse(
        id = id,
        numero = "REP-00$id",
        compromisoId = 30,
        folio = "CP-2026-0101",
        concepto = "Arriendo noviembre",
        empresa = "Plaza Oriente",
        usuarioId = 1,
        nombreUsuario = "Camila Andrea Rojas Peña",
        motivo = ultimoMotivo ?: "YA_PAGUE",
        comentario = "Pagué por transferencia",
        fotoUrl = null,
        estado = estado,
        agenteId = null,
        nombreAgente = null,
        respuesta = null,
        fechaCreacion = "2026-11-17T09:00:00",
        atrasado = false,
        historial = historial
    )

    private val abierto = HistorialResponse(null, "ABIERTO", "Abierto", "2026-11-17T09:00:00", "Camila", "Pagué por transferencia")

    override suspend fun crear(datos: CrearReporteRequest): ReporteResponse {
        ultimoMotivo = datos.motivo
        return reporte(13, "ABIERTO", listOf(abierto))
    }

    override suspend fun listar(
        estado: String?,
        usuarioId: Long?,
        agenteId: Long?,
        buscar: String?,
        soloAtrasados: Boolean,
        pagina: Int,
        tamano: Int
    ): PaginaResponse<ReporteResponse> {
        ultimoEstado = estado
        ultimoAgente = agenteId
        return PaginaResponse(listOf(reporte(13, "ABIERTO", listOf(abierto))), pagina, 1, 1)
    }

    override suspend fun tomar(id: Long, datos: TomarRequest): ReporteResponse {
        val tomado = HistorialResponse("ABIERTO", "EN_GESTION", "En gestión", "2026-11-17T10:00:00", "Andrés", null)
        return reporte(id, "EN_GESTION", listOf(abierto, tomado))
    }

    override suspend fun ultimoDeCompromiso(compromisoId: Long): ReporteResponse {
        if (compromisoId == 99L) throw errorDelServidor(404, "Sin reportes")
        return reporte(13, "ABIERTO", listOf(abierto))
    }

    override suspend fun resumen() = ResumenReportesResponse(2, 1, 1, 6, 2)
    override suspend fun obtener(id: Long): ReporteResponse = TODO()
    override suspend fun resolver(id: Long, datos: ResolverRequest): ReporteResponse = TODO()
    override suspend fun reabrir(id: Long, datos: ReabrirRequest): ReporteResponse = TODO()
    override suspend fun cerrar(id: Long, datos: CerrarRequest): ReporteResponse = TODO()
    override suspend fun subirFoto(id: Long, archivo: MultipartBody.Part): ReporteResponse = TODO()
}

private fun errorDelServidor(codigo: Int, mensaje: String): HttpException =
    HttpException(
        Response.error<Any>(codigo, """{"message":"$mensaje"}""".toResponseBody("application/json".toMediaType()))
    )
