package cl.duocuc.pagoflex.logica

import cl.duocuc.pagoflex.model.Aviso
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.ConfiguracionAvisos
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.ResumenMes
import cl.duocuc.pagoflex.util.Fechas
import java.time.YearMonth

object ReglasNegocio {

    fun compromisoQueBloquea(compromiso: Compromiso, todos: List<Compromiso>): Compromiso? =
        todos.filter {
            it.idEmpresa == compromiso.idEmpresa && it.id != compromiso.id &&
                it.sePuedePagar && it.vencimiento.isBefore(compromiso.vencimiento)
        }.minByOrNull { it.vencimiento }

    fun motivoNoPagable(compromiso: Compromiso, todos: List<Compromiso>): String? {
        when (compromiso.estado) {
            EstadoCompromiso.PAGADO -> return "Este compromiso ya está pagado."
            EstadoCompromiso.ANULADO -> return "La empresa anuló este compromiso, así que no corresponde pagarlo."
            EstadoCompromiso.EN_REVISION -> return "Estamos revisando tu reporte. Podrás pagarlo cuando se resuelva."
            else -> Unit
        }
        val anterior = compromisoQueBloquea(compromiso, todos) ?: return null
        return "Primero paga la cuota ${anterior.cuota} de ${anterior.empresa}, que vence el " +
            "${Fechas.mostrar(anterior.vencimiento)}."
    }

    fun resumenMes(compromisos: List<Compromiso>): ResumenMes {
        val finDeMes = YearMonth.from(Fechas.hoy()).atEndOfMonth()
        val porPagar = compromisos.filter { it.sePuedePagar && !it.vencimiento.isAfter(finDeMes) }
        val proximo = compromisos
            .filter { it.estado == EstadoCompromiso.PENDIENTE }
            .minByOrNull { it.vencimiento }
        return ResumenMes(
            totalMes = porPagar.sumOf { it.totalAPagar },
            proximoVencimiento = proximo?.vencimiento,
            empresaProximo = proximo?.empresa,
            cantidadVencidos = compromisos.count { it.estado == EstadoCompromiso.VENCIDO }
        )
    }

    fun avisos(compromisos: List<Compromiso>, configuracion: ConfiguracionAvisos): List<Aviso> {
        if (!configuracion.activos) return emptyList()
        val avisos = mutableListOf<Aviso>()
        for (compromiso in compromisos.sortedBy { it.vencimiento }) {
            val dias = compromiso.diasParaVencer
            val mensaje = when {
                compromiso.estado == EstadoCompromiso.VENCIDO ->
                    "Te falta \"${compromiso.concepto}\" de ${compromiso.empresa} para quedar al día."
                compromiso.estado != EstadoCompromiso.PENDIENTE -> null
                dias == 0L ->
                    "Hoy vence \"${compromiso.concepto}\" de ${compromiso.empresa}. Es el último día sin recargo."
                dias == 1L ->
                    "Mañana vence \"${compromiso.concepto}\" de ${compromiso.empresa}. ¡Quedarás al día en un minuto!"
                dias >= 2 && dias <= configuracion.diasAnticipacion ->
                    "Faltan $dias días para \"${compromiso.concepto}\" de ${compromiso.empresa}. ¡Vas bien!"
                else -> null
            }
            if (mensaje != null) avisos.add(Aviso(compromiso.id, mensaje))
        }
        return avisos
    }
}
