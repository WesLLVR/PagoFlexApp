package cl.duocuc.pagoflex.model

import java.time.LocalDate
import java.time.YearMonth

data class ResumenMes(
    val totalMes: Int = 0,
    val proximoVencimiento: LocalDate? = null,
    val empresaProximo: String? = null,
    val cantidadVencidos: Int = 0
)

data class Cumplimiento(
    val considerados: Int = 0,
    val aTiempo: Int = 0,
    val porcentaje: Double? = null,
    val categoria: CategoriaCumplimiento = CategoriaCumplimiento.SIN_HISTORIAL
)

data class ResumenCobranza(
    val mes: YearMonth,
    val pagados: Int = 0,
    val pendientes: Int = 0,
    val vencidos: Int = 0,
    val enRevision: Int = 0,
    val anulados: Int = 0,
    val montoRecaudado: Long = 0,
    val tasa: Double? = null
)

data class ResumenReportes(
    val abiertos: Int = 0,
    val enGestion: Int = 0,
    val resueltos: Int = 0,
    val cerrados: Int = 0,
    val atrasados: Int = 0
)

data class Aviso(
    val idCompromiso: Long,
    val mensaje: String
)

data class ConfiguracionAvisos(
    val activos: Boolean = true,
    val diasAnticipacion: Int = 3
)

data class Pagina<T>(
    val contenido: List<T> = emptyList(),
    val pagina: Int = 0,
    val totalPaginas: Int = 1,
    val totalElementos: Long = 0
)
