package cl.duocuc.pagoflex.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Pago(
    val id: Long,
    val numeroComprobante: String,
    val idCompromiso: Long,
    val folio: String,
    val empresa: String,
    val razonSocial: String,
    val concepto: String,
    val cuota: String,
    val vencimiento: LocalDate,
    val fechaHora: LocalDateTime,
    val montoCuota: Int,
    val montoPagado: Int,
    val canal: Canal,
    val medioPago: String,
    val aTiempo: Boolean,
    val nombreUsuario: String,
    val rutUsuario: String
) {
    val recargo: Int get() = montoPagado - montoCuota
}
