package cl.duocuc.pagoflex.model

enum class Rol(val etiqueta: String) {
    USUARIO_FINAL("Usuario final"),
    AGENTE("Agente PagoFlex"),
    EJECUTIVO("Ejecutivo de empresa cliente")
}

enum class EstadoCompromiso(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    VENCIDO("Vencido"),
    PAGADO("Pagado"),
    EN_REVISION("En revisión"),
    ANULADO("Anulado")
}

enum class EstadoReporte(val etiqueta: String) {
    ABIERTO("Abierto"),
    EN_GESTION("En gestión"),
    RESUELTO("Resuelto")
}

enum class MotivoReporte(val etiqueta: String) {
    YA_PAGUE("Ya pagué por otro medio"),
    NO_RECONOZCO("No reconozco este cobro"),
    MONTO_INCORRECTO("El monto no corresponde"),
    OTRO("Otro")
}

enum class MotivoAnulacion(val etiqueta: String) {
    REPACTACION("Repactación"),
    ERROR_REGISTRO("Error de registro"),
    PAGADO_DIRECTO("Pagado directamente a la empresa"),
    OTRO("Otro")
}

enum class MedioPago(val etiqueta: String) {
    DEBITO("Tarjeta de débito"),
    CREDITO("Tarjeta de crédito"),
    TRANSFERENCIA("Transferencia")
}

enum class Canal(val etiqueta: String) {
    APP("App"),
    PORTAL("Portal de pagos"),
    AGENTE("Agente conversacional")
}

enum class CategoriaCumplimiento(val etiqueta: String, val mensaje: String) {
    EJEMPLAR("Ejemplar", "¡Excelente! Tus pagos están impecables."),
    AL_DIA("Al día", "Vas muy bien, sigue así."),
    EN_CAMINO("En camino", "Cada pago a tiempo suma. ¡Tú puedes!"),
    SIN_HISTORIAL("Historial insuficiente", "Aún no tienes pagos suficientes para calcular tu indicador.")
}
