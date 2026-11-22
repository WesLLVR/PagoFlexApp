package cl.duocuc.pagoflex.model

import java.time.LocalDateTime

data class Reporte(
    val id: Long,
    val numero: String,
    val compromisoId: Long,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val motivo: MotivoReporte,
    val comentario: String,
    val fotoUrl: String?,
    val estado: EstadoReporte,
    val agenteId: Long?,
    val nombreAgente: String?,
    val respuesta: String?,
    val fechaCreacion: LocalDateTime,
    val atrasado: Boolean,
    val historial: List<CambioEstado> = emptyList()
)

data class CambioEstado(
    val estado: EstadoReporte,
    val fecha: LocalDateTime,
    val autor: String,
    val comentario: String?
)
