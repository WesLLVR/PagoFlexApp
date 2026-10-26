package cl.duocuc.pagoflex.navigation

sealed class Ruta(val ruta: String) {
    data object Ingreso : Ruta("ingreso")

    data object Inicio : Ruta("inicio")
    data object Historial : Ruta("historial")
    data object Cumplimiento : Ruta("cumplimiento")
    data object Avisos : Ruta("avisos")

    data object Detalle : Ruta("detalle/{id}") {
        fun crear(id: Long) = "detalle/$id"
    }

    data object Pago : Ruta("pago/{id}") {
        fun crear(id: Long) = "pago/$id"
    }

    data object Comprobante : Ruta("comprobante/{id}") {
        fun crear(id: Long) = "comprobante/$id"
    }

    data object Reporte : Ruta("reporte/{id}") {
        fun crear(id: Long) = "reporte/$id"
    }

    data object AgenteReportes : Ruta("agente/reportes")

    data object AgenteDetalle : Ruta("agente/reporte/{id}") {
        fun crear(id: Long) = "agente/reporte/$id"
    }

    data object EjecutivoCompromisos : Ruta("ejecutivo/compromisos")
    data object EjecutivoRegistrar : Ruta("ejecutivo/registrar")
    data object EjecutivoResumen : Ruta("ejecutivo/resumen")

    data object EjecutivoDetalle : Ruta("ejecutivo/compromiso/{id}") {
        fun crear(id: Long) = "ejecutivo/compromiso/$id"
    }
}
