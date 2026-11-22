package cl.duocuc.pagoflex.data.remote

import cl.duocuc.pagoflex.model.CambioEstado
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.ResumenReportes
import cl.duocuc.pagoflex.util.Fechas

data class CrearReporteRequest(
    val compromisoId: Long,
    val usuarioId: Long,
    val motivo: String,
    val comentario: String
)

data class TomarRequest(
    val agenteId: Long
)

data class ResolverRequest(
    val agenteId: Long,
    val respuesta: String
)

data class ReabrirRequest(
    val usuarioId: Long,
    val comentario: String
)

data class CerrarRequest(
    val usuarioId: Long
)

data class HistorialResponse(
    val estadoAnterior: String?,
    val estadoNuevo: String,
    val estadoNombre: String,
    val fecha: String,
    val autor: String,
    val comentario: String?
)

data class ReporteResponse(
    val id: Long,
    val numero: String,
    val compromisoId: Long,
    val folio: String,
    val concepto: String,
    val empresa: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val motivo: String,
    val comentario: String,
    val fotoUrl: String?,
    val estado: String,
    val agenteId: Long?,
    val nombreAgente: String?,
    val respuesta: String?,
    val fechaCreacion: String,
    val atrasado: Boolean,
    val historial: List<HistorialResponse>
)

data class ResumenReportesResponse(
    val abiertos: Int,
    val enGestion: Int,
    val resueltos: Int,
    val cerrados: Int,
    val atrasados: Int
)

fun ReporteResponse.aReporte() = Reporte(
    id = id,
    numero = numero,
    compromisoId = compromisoId,
    folio = folio,
    empresa = empresa,
    concepto = concepto,
    usuarioId = usuarioId,
    nombreUsuario = nombreUsuario,
    motivo = MotivoReporte.valueOf(motivo),
    comentario = comentario,
    fotoUrl = fotoUrl,
    estado = EstadoReporte.valueOf(estado),
    agenteId = agenteId,
    nombreAgente = nombreAgente,
    respuesta = respuesta,
    fechaCreacion = Fechas.desdeApiHora(fechaCreacion),
    atrasado = atrasado,
    historial = historial.map {
        CambioEstado(
            estado = EstadoReporte.valueOf(it.estadoNuevo),
            fecha = Fechas.desdeApiHora(it.fecha),
            autor = it.autor,
            comentario = it.comentario
        )
    }
)

fun ResumenReportesResponse.aResumenReportes() = ResumenReportes(
    abiertos = abiertos,
    enGestion = enGestion,
    resueltos = resueltos,
    cerrados = cerrados,
    atrasados = atrasados
)
