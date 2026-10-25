package cl.duocuc.pagoflex.logica

import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object Validador {

    private const val LARGO_MINIMO_COMENTARIO = 10
    private const val LARGO_MAXIMO_COMENTARIO = 300
    private const val MONTO_MAXIMO = 50_000_000

    private val formatoFecha: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun rut(texto: String): String? {
        val limpio = limpiarRut(texto)
        if (limpio.isEmpty()) return "El RUT es obligatorio"
        if (limpio.length < 8 || limpio.length > 9) return "El RUT debe tener entre 7 y 8 dígitos más el dígito verificador"
        val cuerpo = limpio.dropLast(1)
        val digito = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return "El RUT solo puede tener números antes del guion"
        if (digito != digitoVerificador(cuerpo)) return "El dígito verificador no es válido"
        return null
    }

    fun nombre(texto: String): String? = when {
        texto.isBlank() -> "El nombre es obligatorio"
        texto.trim().length < 3 -> "Escribe el nombre completo"
        else -> null
    }

    fun correo(texto: String): String? = when {
        texto.isBlank() -> "El correo es obligatorio"
        !texto.contains("@") || !texto.substringAfter("@").contains(".") -> "Ingresa un correo válido"
        else -> null
    }

    fun telefono(texto: String): String? {
        val digitos = texto.replace(" ", "").removePrefix("+56")
        return when {
            digitos.isEmpty() -> "El teléfono es obligatorio"
            digitos.length != 9 || !digitos.all { it.isDigit() } -> "Ingresa 9 dígitos, por ejemplo 912345678"
            else -> null
        }
    }

    fun concepto(texto: String): String? = when {
        texto.isBlank() -> "El concepto es obligatorio"
        texto.trim().length < 3 -> "Escribe al menos 3 caracteres"
        texto.trim().length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun monto(texto: String): String? {
        if (texto.isBlank()) return "El monto es obligatorio"
        if (!texto.all { it.isDigit() }) return "Ingresa solo números enteros, sin puntos ni signos"
        val valor = texto.toLongOrNull() ?: return "El monto no es válido"
        return when {
            valor <= 0 -> "El monto debe ser mayor que cero"
            valor > MONTO_MAXIMO -> "El monto supera el máximo permitido"
            else -> null
        }
    }

    fun fechaVencimiento(texto: String): String? {
        if (texto.isBlank()) return "La fecha de vencimiento es obligatoria"
        val fecha = fechaDesdeTexto(texto) ?: return "Usa el formato DD-MM-AAAA"
        return if (fecha.isBefore(Fechas.hoy())) "La fecha no puede ser anterior a hoy" else null
    }

    fun cuotas(cuota: String, total: String): Pair<String?, String?> {
        if (cuota.isBlank() && total.isBlank()) return null to null
        val numeroCuota = cuota.toIntOrNull()
        val totalCuotas = total.toIntOrNull()
        val errorCuota = when {
            cuota.isBlank() -> "Indica el número de cuota"
            numeroCuota == null || numeroCuota <= 0 -> "Debe ser un número mayor que cero"
            totalCuotas != null && numeroCuota > totalCuotas -> "La cuota no puede ser mayor que el total"
            else -> null
        }
        val errorTotal = when {
            total.isBlank() -> "Indica el total de cuotas"
            totalCuotas == null || totalCuotas <= 0 -> "Debe ser un número mayor que cero"
            else -> null
        }
        return errorCuota to errorTotal
    }

    fun medioPago(medio: MedioPago?): String? =
        if (medio == null) "Elige cómo quieres pagar" else null

    fun motivoReporte(motivo: MotivoReporte?): String? =
        if (motivo == null) "Elige el motivo del problema" else null

    fun comentarioReporte(motivo: MotivoReporte?, comentario: String): String? = when {
        comentario.length > LARGO_MAXIMO_COMENTARIO -> "Máximo $LARGO_MAXIMO_COMENTARIO caracteres"
        motivo == MotivoReporte.OTRO && comentario.trim().length < LARGO_MINIMO_COMENTARIO ->
            "Si eliges \"Otro\", cuéntanos el problema (mínimo $LARGO_MINIMO_COMENTARIO caracteres)"
        else -> null
    }

    fun motivoAnulacion(motivo: MotivoAnulacion?): String? =
        if (motivo == null) "El motivo es obligatorio" else null

    fun comentarioAnulacion(motivo: MotivoAnulacion?, comentario: String): String? =
        if (motivo == MotivoAnulacion.OTRO && comentario.trim().length < LARGO_MINIMO_COMENTARIO)
            "Si eliges \"Otro\", explica el motivo (mínimo $LARGO_MINIMO_COMENTARIO caracteres)" else null

    fun respuestaAgente(texto: String): String? = when {
        texto.isBlank() -> "Escribe la respuesta para el usuario antes de resolver"
        texto.trim().length < LARGO_MINIMO_COMENTARIO -> "La respuesta debe tener al menos $LARGO_MINIMO_COMENTARIO caracteres"
        texto.length > LARGO_MAXIMO_COMENTARIO -> "Máximo $LARGO_MAXIMO_COMENTARIO caracteres"
        else -> null
    }

    fun fechaDesdeTexto(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), formatoFecha)
    } catch (e: Exception) {
        null
    }

    fun limpiarRut(texto: String): String =
        texto.replace(".", "").replace("-", "").replace(" ", "").uppercase()

    fun formatearRut(texto: String): String {
        val limpio = limpiarRut(texto)
        if (limpio.length < 2) return limpio
        val cuerpo = limpio.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return "$cuerpo-${limpio.last()}"
    }

    private fun digitoVerificador(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val resultado = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.digitToChar()
        }
    }
}
