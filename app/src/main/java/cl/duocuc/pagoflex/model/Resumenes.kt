package cl.duocuc.pagoflex.model

import java.time.LocalDate

data class ResumenMes(
    val totalMes: Int = 0,
    val proximoVencimiento: LocalDate? = null,
    val empresaProximo: String? = null,
    val cantidadVencidos: Int = 0
)

data class Cumplimiento(
    val considerados: Int = 0,
    val aTiempo: Int = 0
) {
    val porcentaje: Double?
        get() = if (considerados < 3) null else aTiempo * 100.0 / considerados

    val categoria: CategoriaCumplimiento
        get() {
            val valor = porcentaje ?: return CategoriaCumplimiento.SIN_HISTORIAL
            return when {
                valor >= 95.0 -> CategoriaCumplimiento.EJEMPLAR
                valor >= 80.0 -> CategoriaCumplimiento.AL_DIA
                else -> CategoriaCumplimiento.EN_CAMINO
            }
        }
}

data class ResumenCobranza(
    val pagados: Int = 0,
    val pendientes: Int = 0,
    val vencidos: Int = 0,
    val enRevision: Int = 0,
    val anulados: Int = 0,
    val montoRecaudado: Int = 0
) {
    val considerados: Int get() = pagados + pendientes + vencidos + enRevision

    val tasa: Double?
        get() = if (considerados == 0) null else pagados * 100.0 / considerados
}

data class Aviso(
    val idCompromiso: Long,
    val mensaje: String
)

data class ConfiguracionAvisos(
    val activos: Boolean = true,
    val diasAnticipacion: Int = 3
)
