package cl.duocuc.pagoflex.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Fechas {
    private val formatoBd: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val formatoBdHora: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val formatoPantalla: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val formatoPantallaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    fun hoy(): LocalDate = LocalDate.now()

    fun ahora(): LocalDateTime = LocalDateTime.now().withSecond(0).withNano(0)

    fun aBd(fecha: LocalDate): String = fecha.format(formatoBd)

    fun aBd(fechaHora: LocalDateTime): String = fechaHora.format(formatoBdHora)

    fun desdeBd(texto: String): LocalDate = LocalDate.parse(texto, formatoBd)

    fun desdeBdHora(texto: String): LocalDateTime = LocalDateTime.parse(texto, formatoBdHora)

    fun mostrar(fecha: LocalDate): String = fecha.format(formatoPantalla)

    fun mostrar(fechaHora: LocalDateTime): String = fechaHora.format(formatoPantallaHora)

    fun diasEntre(desde: LocalDate, hasta: LocalDate): Long = ChronoUnit.DAYS.between(desde, hasta)

    fun diasHabilesEntre(desde: LocalDate, hasta: LocalDate): Int {
        var dias = 0
        var fecha = desde.plusDays(1)
        while (!fecha.isAfter(hasta)) {
            if (fecha.dayOfWeek != DayOfWeek.SATURDAY && fecha.dayOfWeek != DayOfWeek.SUNDAY) dias++
            fecha = fecha.plusDays(1)
        }
        return dias
    }
}
