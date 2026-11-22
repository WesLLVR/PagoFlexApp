package cl.duocuc.pagoflex.model

import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate

data class Compromiso(
    val id: Long,
    val folio: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val rutUsuario: String,
    val empresaId: Long,
    val empresa: String,
    val razonSocial: String,
    val concepto: String,
    val numeroCuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val recargo: Int,
    val totalAPagar: Int,
    val vencimiento: LocalDate,
    val estado: EstadoCompromiso,
    val diasAtraso: Long,
    val motivoAnulacion: String? = null,
    val comentarioAnulacion: String? = null
) {
    val cuota: String
        get() = if (numeroCuota != null && totalCuotas != null) "$numeroCuota/$totalCuotas" else "—"

    val diasParaVencer: Long
        get() = Fechas.diasEntre(Fechas.hoy(), vencimiento)

    val sePuedePagar: Boolean
        get() = estado == EstadoCompromiso.PENDIENTE || estado == EstadoCompromiso.VENCIDO

    val sePuedeAnular: Boolean
        get() = estado != EstadoCompromiso.PAGADO && estado != EstadoCompromiso.ANULADO
}
