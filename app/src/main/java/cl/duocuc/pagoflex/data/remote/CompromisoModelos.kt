package cl.duocuc.pagoflex.data.remote

import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Empresa
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.ResumenCobranza
import cl.duocuc.pagoflex.model.ResumenMes
import cl.duocuc.pagoflex.util.Fechas
import java.time.YearMonth

data class CompromisoResponse(
    val id: Long,
    val folio: String,
    val usuarioId: Long,
    val nombreUsuario: String,
    val rutUsuario: String,
    val empresaId: Long,
    val empresa: String,
    val razonSocial: String,
    val deuda: String,
    val concepto: String,
    val numeroCuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val recargo: Int,
    val totalAPagar: Int,
    val vencimiento: String,
    val estado: String,
    val diasAtraso: Long,
    val motivoAnulacion: String?,
    val comentarioAnulacion: String?
)

data class PagableResponse(
    val pagable: Boolean,
    val motivo: String?
)

data class ResumenMesResponse(
    val totalMes: Int,
    val proximoVencimiento: String?,
    val empresaProximo: String?,
    val cantidadVencidos: Int
)

data class ResumenCobranzaResponse(
    val mes: String,
    val pagados: Int,
    val pendientes: Int,
    val vencidos: Int,
    val enRevision: Int,
    val anulados: Int,
    val montoRecaudado: Long,
    val tasa: Double?
)

data class EmpresaResponse(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val razonSocial: String,
    val recargo: Int
)

data class RegistroCompromisoRequest(
    val empresaId: Long,
    val ejecutivoId: Long,
    val rut: String,
    val nombre: String?,
    val correo: String?,
    val telefono: String?,
    val concepto: String,
    val numeroCuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val vencimiento: String
)

data class AnularRequest(
    val motivo: String,
    val comentario: String,
    val ejecutivoId: Long
)

fun CompromisoResponse.aCompromiso() = Compromiso(
    id = id,
    folio = folio,
    usuarioId = usuarioId,
    nombreUsuario = nombreUsuario,
    rutUsuario = rutUsuario,
    empresaId = empresaId,
    empresa = empresa,
    razonSocial = razonSocial,
    concepto = concepto,
    numeroCuota = numeroCuota,
    totalCuotas = totalCuotas,
    monto = monto,
    recargo = recargo,
    totalAPagar = totalAPagar,
    vencimiento = Fechas.desdeApi(vencimiento),
    estado = EstadoCompromiso.valueOf(estado),
    diasAtraso = diasAtraso,
    motivoAnulacion = motivoAnulacion,
    comentarioAnulacion = comentarioAnulacion
)

fun ResumenMesResponse.aResumenMes() = ResumenMes(
    totalMes = totalMes,
    proximoVencimiento = proximoVencimiento?.let { Fechas.desdeApi(it) },
    empresaProximo = empresaProximo,
    cantidadVencidos = cantidadVencidos
)

fun ResumenCobranzaResponse.aResumenCobranza() = ResumenCobranza(
    mes = YearMonth.parse(mes),
    pagados = pagados,
    pendientes = pendientes,
    vencidos = vencidos,
    enRevision = enRevision,
    anulados = anulados,
    montoRecaudado = montoRecaudado,
    tasa = tasa
)

fun EmpresaResponse.aEmpresa() = Empresa(
    id = id,
    nombre = nombre,
    razonSocial = razonSocial,
    recargo = recargo
)
