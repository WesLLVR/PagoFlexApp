package cl.duocuc.pagoflex

import cl.duocuc.pagoflex.data.remote.AnularRequest
import cl.duocuc.pagoflex.data.remote.CompromisoApi
import cl.duocuc.pagoflex.data.remote.CompromisoResponse
import cl.duocuc.pagoflex.data.remote.EmpresaResponse
import cl.duocuc.pagoflex.data.remote.PagableResponse
import cl.duocuc.pagoflex.data.remote.PaginaResponse
import cl.duocuc.pagoflex.data.remote.RegistroCompromisoRequest
import cl.duocuc.pagoflex.data.remote.ResumenCobranzaResponse
import cl.duocuc.pagoflex.data.remote.ResumenMesResponse
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CompromisosTest {

    private lateinit var apiFalsa: FakeCompromisoApi
    private lateinit var repositorio: CompromisoRepository

    @Before
    fun preparar() {
        apiFalsa = FakeCompromisoApi()
        repositorio = CompromisoRepository(api = apiFalsa)
    }

    @Test
    fun listarDeUsuario_conBusquedaVacia_enviaNullALaApi() = runBlocking {
        val pagina = repositorio.listarDeUsuario(1, "VIGENTE", "   ", 0)

        assertNull(apiFalsa.ultimoBuscar)
        assertEquals("VIGENTE", apiFalsa.ultimoEstado)
        assertEquals(2, pagina.contenido.size)
        assertEquals(3, pagina.totalPaginas)
    }

    @Test
    fun listar_convierteEstadoYFecha() = runBlocking {
        val compromiso = repositorio.listarDeUsuario(1, null, "", 0).contenido[1]

        assertEquals(EstadoCompromiso.VENCIDO, compromiso.estado)
        assertEquals(LocalDate.of(2026, 11, 20), compromiso.vencimiento)
        assertEquals("10/12", compromiso.cuota)
    }

    @Test
    fun motivoNoPagable_devuelveElMotivoSoloSiNoSePuedePagar() = runBlocking {
        assertNull(repositorio.motivoNoPagable(31))

        apiFalsa.pagable = PagableResponse(false, "Primero paga la cuota 9/12")

        assertEquals("Primero paga la cuota 9/12", repositorio.motivoNoPagable(31))
    }

    @Test
    fun resumenMes_convierteLaFechaDelProximoVencimiento() = runBlocking {
        val resumen = repositorio.resumenMes(1)

        assertEquals(140000, resumen.totalMes)
        assertEquals(LocalDate.of(2026, 11, 20), resumen.proximoVencimiento)
    }

    @Test
    fun rutConDigitoCorrectoEsValido() {
        assertNull(Validador.rut("15.834.207-3"))
        assertNull(Validador.rut("16248317-k"))
    }

    @Test
    fun rutConDigitoIncorrectoEsRechazado() {
        assertNotNull(Validador.rut("15.834.207-4"))
        assertNotNull(Validador.rut(""))
    }

    @Test
    fun formatearRut_agregaPuntosYGuion() {
        assertEquals("15.834.207-3", Validador.formatearRut("158342073"))
    }

    @Test
    fun montoDebeSerEnteroYPositivo() {
        assertNull(Validador.monto("310000"))
        assertNotNull(Validador.monto("0"))
        assertNotNull(Validador.monto("31.000"))
    }

    @Test
    fun fechaVencimiento_noPuedeSerAnteriorAHoyNiVacia() {
        assertNotNull(Validador.fechaVencimiento(null))
        assertNotNull(Validador.fechaVencimiento(Fechas.hoy().minusDays(1)))
        assertNull(Validador.fechaVencimiento(Fechas.hoy().plusDays(5)))
    }

    @Test
    fun rangoFechas_desdeDebeSerAnteriorAHasta() {
        val hoy = Fechas.hoy()
        assertNotNull(Validador.rangoFechas(hoy, hoy.minusDays(3)))
        assertNull(Validador.rangoFechas(hoy.minusDays(3), hoy))
        assertNull(Validador.rangoFechas(null, hoy))
    }

    @Test
    fun cuotaNoPuedeSuperarElTotal() {
        assertNotNull(Validador.cuotas("37", "36").first)
        assertNull(Validador.cuotas("16", "36").first)
    }
}

private fun compromisoResponse(id: Long, estado: String = "PENDIENTE") = CompromisoResponse(
    id = id,
    folio = "CP-2026-$id",
    usuarioId = 1,
    nombreUsuario = "Camila Andrea Rojas Peña",
    rutUsuario = "15.834.207-3",
    empresaId = 3,
    empresa = "Club Deportivo Los Leones",
    razonSocial = "Club Los Leones SpA",
    deuda = "Mensualidad club",
    concepto = "Mensualidad club",
    numeroCuota = 10,
    totalCuotas = 12,
    monto = 25000,
    recargo = 0,
    totalAPagar = 25000,
    vencimiento = "2026-11-20",
    estado = estado,
    diasAtraso = 0,
    motivoAnulacion = null,
    comentarioAnulacion = null
)

private class FakeCompromisoApi : CompromisoApi {

    var ultimoEstado: String? = "sin llamar"
    var ultimoBuscar: String? = "sin llamar"
    var pagable = PagableResponse(true, null)

    override suspend fun listar(
        usuarioId: Long?,
        empresaId: Long?,
        estado: String?,
        buscar: String?,
        mes: String?,
        pagina: Int,
        tamano: Int
    ): PaginaResponse<CompromisoResponse> {
        ultimoEstado = estado
        ultimoBuscar = buscar
        return PaginaResponse(listOf(compromisoResponse(30), compromisoResponse(31, "VENCIDO")), pagina, 3, 12)
    }

    override suspend fun pagable(id: Long): PagableResponse = pagable

    override suspend fun resumenMes(usuarioId: Long) = ResumenMesResponse(140000, "2026-11-20", "Club Deportivo Los Leones", 1)

    override suspend fun obtener(id: Long): CompromisoResponse = compromisoResponse(id)
    override suspend fun cuotas(id: Long): List<CompromisoResponse> = TODO()
    override suspend fun proximos(usuarioId: Long, dias: Int): List<CompromisoResponse> = TODO()
    override suspend fun resumenCobranza(empresaId: Long, mes: String): ResumenCobranzaResponse = TODO()
    override suspend fun registrar(datos: RegistroCompromisoRequest): CompromisoResponse = TODO()
    override suspend fun anular(id: Long, datos: AnularRequest): CompromisoResponse = TODO()
    override suspend fun eliminar(id: Long) = TODO()
    override suspend fun empresa(id: Long): EmpresaResponse = TODO()
}
