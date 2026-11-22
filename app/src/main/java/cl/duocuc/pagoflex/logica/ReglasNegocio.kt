package cl.duocuc.pagoflex.logica

import cl.duocuc.pagoflex.model.Aviso
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.ConfiguracionAvisos
import cl.duocuc.pagoflex.model.EstadoCompromiso

object ReglasNegocio {

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
                dias in 2..configuracion.diasAnticipacion ->
                    "Faltan $dias días para \"${compromiso.concepto}\" de ${compromiso.empresa}. ¡Vas bien!"
                else -> null
            }
            if (mensaje != null) avisos.add(Aviso(compromiso.id, mensaje))
        }
        return avisos
    }
}
