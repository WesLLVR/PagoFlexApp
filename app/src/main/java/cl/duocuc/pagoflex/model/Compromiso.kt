package cl.duocuc.pagoflex.model

import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate

data class Compromiso(
    val id: Long,
    val folio: String,
    val idUsuario: Long,
    val nombreUsuario: String,
    val rutUsuario: String,
    val idEmpresa: Long,
    val empresa: String,
    val concepto: String,
    val numeroCuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val vencimiento: LocalDate,
    val estadoGuardado: EstadoCompromiso,
    val recargoEmpresa: Int,
    val motivoAnulacion: String? = null
) {
    val estado: EstadoCompromiso
        get() = if (estadoGuardado == EstadoCompromiso.PENDIENTE && vencimiento.isBefore(Fechas.hoy()))
            EstadoCompromiso.VENCIDO else estadoGuardado

    val diasAtraso: Long
        get() = if (estado == EstadoCompromiso.VENCIDO) Fechas.diasEntre(vencimiento, Fechas.hoy()) else 0

    val diasParaVencer: Long
        get() = Fechas.diasEntre(Fechas.hoy(), vencimiento)

    val recargo: Int
        get() = if (estado == EstadoCompromiso.VENCIDO) recargoEmpresa else 0

    val totalAPagar: Int
        get() = monto + recargo

    val cuota: String
        get() = if (numeroCuota != null && totalCuotas != null) "$numeroCuota/$totalCuotas" else "—"

    val sePuedePagar: Boolean
        get() = estado == EstadoCompromiso.PENDIENTE || estado == EstadoCompromiso.VENCIDO

    val sePuedeAnular: Boolean
        get() = estado != EstadoCompromiso.PAGADO && estado != EstadoCompromiso.ANULADO
}
