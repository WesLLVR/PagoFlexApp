package cl.duocuc.pagoflex.data.repository

import cl.duocuc.pagoflex.data.remote.PagoApi
import cl.duocuc.pagoflex.data.remote.PagoRequest
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.remote.aCumplimiento
import cl.duocuc.pagoflex.data.remote.aPagina
import cl.duocuc.pagoflex.data.remote.aPago
import cl.duocuc.pagoflex.data.remote.aPagoRealizado
import cl.duocuc.pagoflex.data.remote.llamar
import cl.duocuc.pagoflex.data.remote.llamarONulo
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.model.PagoRealizado
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate

class PagoRepository(
    private val api: PagoApi = RetrofitClient.crear(PagoApi::class.java)
) {

    suspend fun pagar(compromisoId: Long, medio: MedioPago): PagoRealizado =
        llamar { api.pagar(PagoRequest(compromisoId, medio.etiqueta)) }.aPagoRealizado()

    suspend fun listar(
        usuarioId: Long,
        empresa: String?,
        desde: LocalDate?,
        hasta: LocalDate?,
        buscar: String,
        pagina: Int,
        tamano: Int = 5
    ): Pagina<Pago> = llamar {
        api.listar(
            usuarioId = usuarioId,
            empresa = empresa,
            desde = desde?.let { Fechas.aApi(it) },
            hasta = hasta?.let { Fechas.aApi(it) },
            buscar = buscar.trim().ifBlank { null },
            pagina = pagina,
            tamano = tamano
        )
    }.aPagina { it.aPago() }

    suspend fun empresas(usuarioId: Long): List<String> =
        llamar { api.empresas(usuarioId) }

    suspend fun cumplimiento(usuarioId: Long): Cumplimiento =
        llamar { api.cumplimiento(usuarioId) }.aCumplimiento()

    suspend fun pagoDeCompromiso(compromisoId: Long): Pago? =
        llamarONulo { api.porCompromiso(compromisoId) }?.aPago()
}
