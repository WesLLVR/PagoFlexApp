package cl.duocuc.pagoflex.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Fechas {
    private val formatoPantalla: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val formatoPantallaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

    fun hoy(): LocalDate = LocalDate.now()

    fun desdeApi(texto: String): LocalDate = LocalDate.parse(texto)

    fun desdeApiHora(texto: String): LocalDateTime = LocalDateTime.parse(texto)

    fun aApi(fecha: LocalDate): String = fecha.toString()

    fun mostrar(fecha: LocalDate): String = fecha.format(formatoPantalla)

    fun mostrar(fechaHora: LocalDateTime): String = fechaHora.format(formatoPantallaHora)

    fun diasEntre(desde: LocalDate, hasta: LocalDate): Long = ChronoUnit.DAYS.between(desde, hasta)

    fun desdeMilisegundos(milisegundos: Long): LocalDate =
        Instant.ofEpochMilli(milisegundos).atZone(ZoneOffset.UTC).toLocalDate()

    fun aMilisegundos(fecha: LocalDate): Long =
        fecha.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}
