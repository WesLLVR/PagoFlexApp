package cl.duocuc.pagoflex.util

import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object Formato {
    private val chile: Locale = Locale.forLanguageTag("es-CL")

    fun pesos(monto: Int): String = "$ " + String.format(chile, "%,d", monto)

    fun porcentaje(valor: Double): String = String.format(chile, "%.1f%%", valor)

    fun rutOculto(rut: String): String {
        val partes = rut.split("-")
        if (partes.size != 2) return rut
        val inicio = partes[0].substringBefore(".")
        return "$inicio.*.*-${partes[1]}"
    }

    fun mes(mes: YearMonth): String {
        val nombre = mes.month.getDisplayName(TextStyle.FULL, chile)
        return nombre.replaceFirstChar { it.uppercase() } + " " + mes.year
    }
}
