package cl.duocuc.pagoflex.data.remote

import cl.duocuc.pagoflex.model.CategoriaCumplimiento
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.model.PagoRealizado
import cl.duocuc.pagoflex.util.Fechas

data class PagoRequest(
    val compromisoId: Long,
    val medioPago: String
)

data class PagoResponse(
    val id: Long,
    val numeroComprobante: String,
    val compromisoId: Long,
    val usuarioId: Long,
    val nombreUsuario: String,
    val rutUsuario: String,
    val folio: String,
    val empresa: String,
    val razonSocial: String,
    val concepto: String,
    val numeroCuota: Int?,
    val totalCuotas: Int?,
    val montoCuota: Int,
    val vencimiento: String,
    val fechaHora: String,
    val montoPagado: Int,
    val canal: String,
    val canalNombre: String,
    val medioPago: String,
    val aTiempo: Boolean
)

data class PagoRealizadoResponse(
    val pago: PagoResponse,
    val siguienteCuota: CompromisoResponse?
)

data class CumplimientoResponse(
    val considerados: Int,
    val aTiempo: Int,
    val porcentaje: Double?,
    val categoria: String
)

fun PagoResponse.aPago() = Pago(
    id = id,
    numeroComprobante = numeroComprobante,
    compromisoId = compromisoId,
    folio = folio,
    empresa = empresa,
    razonSocial = razonSocial,
    concepto = concepto,
    cuota = if (numeroCuota != null && totalCuotas != null) "$numeroCuota/$totalCuotas" else "—",
    vencimiento = Fechas.desdeApi(vencimiento),
    fechaHora = Fechas.desdeApiHora(fechaHora),
    montoCuota = montoCuota,
    montoPagado = montoPagado,
    canal = canalNombre,
    medioPago = medioPago,
    aTiempo = aTiempo,
    nombreUsuario = nombreUsuario,
    rutUsuario = rutUsuario
)

fun PagoRealizadoResponse.aPagoRealizado() = PagoRealizado(
    pago = pago.aPago(),
    siguienteCuota = siguienteCuota?.aCompromiso()
)

fun CumplimientoResponse.aCumplimiento() = Cumplimiento(
    considerados = considerados,
    aTiempo = aTiempo,
    porcentaje = porcentaje,
    categoria = CategoriaCumplimiento.valueOf(categoria)
)
