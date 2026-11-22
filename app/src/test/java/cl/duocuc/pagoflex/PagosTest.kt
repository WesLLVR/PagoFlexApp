package cl.duocuc.pagoflex

import cl.duocuc.pagoflex.data.remote.CompromisoResponse
import cl.duocuc.pagoflex.data.remote.CumplimientoResponse
import cl.duocuc.pagoflex.data.remote.PaginaResponse
import cl.duocuc.pagoflex.data.remote.PagoApi
import cl.duocuc.pagoflex.data.remote.PagoRealizadoResponse
import cl.duocuc.pagoflex.data.remote.PagoRequest
import cl.duocuc.pagoflex.data.remote.PagoResponse
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.logica.ReglasNegocio
import cl.duocuc.pagoflex.model.CategoriaCumplimiento
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.ConfiguracionAvisos
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class PagosTest {

    private lateinit var apiFalsa: FakePagoApi
    private lateinit var repositorio: PagoRepository

    @Before
    fun preparar() {
        apiFalsa = FakePagoApi()
        repositorio = PagoRepository(api = apiFalsa)
    }

    @Test
    fun pagar_enviaElNombreDelMedioYDevuelveLaSiguienteCuota() = runBlocking {
        val resultado = repositorio.pagar(30, MedioPago.DEBITO)

        assertEquals("Tarjeta de débito", apiFalsa.ultimoMedio)
        assertEquals("CPR-000454", resultado.pago.numeroComprobante)
        assertEquals(31L, resultado.siguienteCuota?.id)
    }

    @Test
    fun pago_calculaElRecargoComoDiferencia() = runBlocking {
        val pago = repositorio.pagoDeCompromiso(30)!!

        assertEquals(2500, pago.recargo)
        assertEquals("10/12", pago.cuota)
    }

    @Test
    fun pagoDeCompromiso_sinPago_devuelveNull() = runBlocking {
        assertNull(repositorio.pagoDeCompromiso(99))
    }

    @Test
    fun cumplimiento_convierteLaCategoria() = runBlocking {
        val cumplimiento = repositorio.cumplimiento(1)

        assertEquals(CategoriaCumplimiento.AL_DIA, cumplimiento.categoria)
        assertEquals(90.0, cumplimiento.porcentaje!!, 0.01)
    }

    private fun compromiso(id: Long, diasParaVencer: Long, estado: EstadoCompromiso) = Compromiso(
        id = id,
        folio = "CP-2026-$id",
        usuarioId = 1,
        nombreUsuario = "Camila Rojas",
        rutUsuario = "15.834.207-3",
        empresaId = 1,
        empresa = "Club Deportivo",
        razonSocial = "Club Deportivo SpA",
        concepto = "Mensualidad",
        numeroCuota = null,
        totalCuotas = null,
        monto = 25000,
        recargo = 0,
        totalAPagar = 25000,
        vencimiento = Fechas.hoy().plusDays(diasParaVencer),
        estado = estado,
        diasAtraso = 0
    )

    @Test
    fun avisos_mensajeSegunDiasQueFaltan() {
        val avisos = ReglasNegocio.avisos(
            listOf(
                compromiso(1, 0, EstadoCompromiso.PENDIENTE),
                compromiso(2, 1, EstadoCompromiso.PENDIENTE),
                compromiso(3, -2, EstadoCompromiso.VENCIDO)
            ),
            ConfiguracionAvisos(activos = true, diasAnticipacion = 3)
        )

        assertEquals(3, avisos.size)
        assertTrue(avisos.first { it.idCompromiso == 1L }.mensaje.startsWith("Hoy vence"))
        assertTrue(avisos.first { it.idCompromiso == 2L }.mensaje.startsWith("Mañana vence"))
        assertTrue(avisos.first { it.idCompromiso == 3L }.mensaje.startsWith("Te falta"))
    }

    @Test
    fun avisos_respetaLaAnticipacionElegida() {
        val lejano = compromiso(4, 5, EstadoCompromiso.PENDIENTE)

        assertTrue(ReglasNegocio.avisos(listOf(lejano), ConfiguracionAvisos(true, 3)).isEmpty())
        assertEquals(1, ReglasNegocio.avisos(listOf(lejano), ConfiguracionAvisos(true, 5)).size)
    }

    @Test
    fun avisos_desactivadosNoGeneranNada() {
        val avisos = ReglasNegocio.avisos(
            listOf(compromiso(1, 0, EstadoCompromiso.PENDIENTE)),
            ConfiguracionAvisos(activos = false)
        )

        assertTrue(avisos.isEmpty())
    }
}

private class FakePagoApi : PagoApi {

    var ultimoMedio: String? = null

    private fun pago(compromisoId: Long) = PagoResponse(
        id = 40,
        numeroComprobante = "CPR-000454",
        compromisoId = compromisoId,
        usuarioId = 1,
        nombreUsuario = "Camila Andrea Rojas Peña",
        rutUsuario = "15.834.207-3",
        folio = "CP-2026-$compromisoId",
        empresa = "Club Deportivo Los Leones",
        razonSocial = "Club Los Leones SpA",
        concepto = "Mensualidad club",
        numeroCuota = 10,
        totalCuotas = 12,
        montoCuota = 25000,
        vencimiento = "2026-11-20",
        fechaHora = "2026-11-17T10:30:00",
        montoPagado = 27500,
        canal = "APP",
        canalNombre = "App",
        medioPago = ultimoMedio ?: "",
        aTiempo = false
    )

    override suspend fun pagar(datos: PagoRequest): PagoRealizadoResponse {
        ultimoMedio = datos.medioPago
        return PagoRealizadoResponse(pago(datos.compromisoId), compromisoResponse(datos.compromisoId + 1))
    }

    override suspend fun porCompromiso(compromisoId: Long): PagoResponse {
        if (compromisoId == 99L) throw errorDelServidor(404, "Sin pago")
        return pago(compromisoId)
    }

    override suspend fun cumplimiento(usuarioId: Long) = CumplimientoResponse(10, 9, 90.0, "AL_DIA")

    override suspend fun listar(
        usuarioId: Long,
        empresa: String?,
        desde: String?,
        hasta: String?,
        buscar: String?,
        pagina: Int,
        tamano: Int
    ): PaginaResponse<PagoResponse> = TODO()

    override suspend fun empresas(usuarioId: Long): List<String> = TODO()
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

private fun errorDelServidor(codigo: Int, mensaje: String): HttpException =
    HttpException(
        Response.error<Any>(codigo, """{"message":"$mensaje"}""".toResponseBody("application/json".toMediaType()))
    )
