package cl.duocuc.pagoflex.model

import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate

data class Reporte(
    val id: Long,
    val numero: String,
    val idCompromiso: Long,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val nombreUsuario: String,
    val motivo: MotivoReporte,
    val comentario: String,
    val foto: ByteArray?,
    val fecha: LocalDate,
    val estado: EstadoReporte,
    val nombreAgente: String?,
    val respuesta: String?,
    val fechaResolucion: LocalDate?
) {
    val atrasado: Boolean
        get() = estado == EstadoReporte.ABIERTO && Fechas.diasHabilesEntre(fecha, Fechas.hoy()) > 2
}
