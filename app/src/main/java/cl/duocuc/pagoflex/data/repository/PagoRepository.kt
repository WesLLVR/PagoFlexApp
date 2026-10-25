package cl.duocuc.pagoflex.data.repository

import android.content.ContentValues
import android.database.Cursor
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_COMPROMISOS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_EMPRESAS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_PAGOS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_USUARIOS
import cl.duocuc.pagoflex.model.Canal
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.util.Fechas
import java.util.Locale

class PagoRepository(private val db: PagoFlexDbHelper) {

    companion object {
        private const val SQL_SELECT =
            "SELECT p.*, c.folio, c.concepto, c.numero_cuota, c.total_cuotas, c.monto, " +
                "c.fecha_vencimiento, e.nombre_fantasia, e.razon_social, u.nombre, u.rut " +
                "FROM $TABLA_PAGOS p " +
                "INNER JOIN $TABLA_COMPROMISOS c ON p.id_compromiso = c.id_compromiso " +
                "INNER JOIN $TABLA_EMPRESAS e ON c.id_empresa = e.id_empresa " +
                "INNER JOIN $TABLA_USUARIOS u ON c.id_usuario = u.id_usuario"
    }

    fun pagar(compromiso: Compromiso, medio: MedioPago): Long {
        val baseDatos = db.writableDatabase
        val ahora = Fechas.ahora()
        baseDatos.beginTransaction()
        try {
            val valores = ContentValues().apply {
                put("numero_comprobante", siguienteComprobante())
                put("id_compromiso", compromiso.id)
                put("fecha_hora", Fechas.aBd(ahora))
                put("monto_pagado", compromiso.totalAPagar)
                put("canal", Canal.APP.name)
                put("medio_pago", medio.etiqueta)
                put("a_tiempo", if (ahora.toLocalDate().isAfter(compromiso.vencimiento)) 0 else 1)
            }
            val idPago = baseDatos.insert(TABLA_PAGOS, null, valores)
            val estado = ContentValues().apply { put("estado", EstadoCompromiso.PAGADO.name) }
            baseDatos.update(TABLA_COMPROMISOS, estado, "id_compromiso = ?", arrayOf(compromiso.id.toString()))
            baseDatos.setTransactionSuccessful()
            return idPago
        } finally {
            baseDatos.endTransaction()
        }
    }

    fun buscarPorCompromiso(idCompromiso: Long): Pago? =
        consultar("$SQL_SELECT WHERE p.id_compromiso = ?", arrayOf(idCompromiso.toString())).firstOrNull()

    fun historialDeUsuario(idUsuario: Long): List<Pago> =
        consultar("$SQL_SELECT WHERE c.id_usuario = ? ORDER BY p.fecha_hora DESC", arrayOf(idUsuario.toString()))

    private fun siguienteComprobante(): String {
        val ultimo = db.readableDatabase.rawQuery(
            "SELECT MAX(numero_comprobante) FROM $TABLA_PAGOS", null
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getString(0) else "CPR-000000"
        }
        val numero = ultimo.removePrefix("CPR-").toInt() + 1
        return String.format(Locale.ROOT, "CPR-%06d", numero)
    }

    private fun consultar(sql: String, args: Array<String>): List<Pago> {
        val lista = mutableListOf<Pago>()
        db.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        return lista
    }

    private fun leer(cursor: Cursor): Pago {
        val columnaCuota = cursor.getColumnIndexOrThrow("numero_cuota")
        val columnaTotal = cursor.getColumnIndexOrThrow("total_cuotas")
        val cuota = if (cursor.isNull(columnaCuota) || cursor.isNull(columnaTotal)) "—"
            else "${cursor.getInt(columnaCuota)}/${cursor.getInt(columnaTotal)}"
        return Pago(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id_pago")),
            numeroComprobante = cursor.getString(cursor.getColumnIndexOrThrow("numero_comprobante")),
            idCompromiso = cursor.getLong(cursor.getColumnIndexOrThrow("id_compromiso")),
            folio = cursor.getString(cursor.getColumnIndexOrThrow("folio")),
            empresa = cursor.getString(cursor.getColumnIndexOrThrow("nombre_fantasia")),
            razonSocial = cursor.getString(cursor.getColumnIndexOrThrow("razon_social")),
            concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
            cuota = cuota,
            vencimiento = Fechas.desdeBd(cursor.getString(cursor.getColumnIndexOrThrow("fecha_vencimiento"))),
            fechaHora = Fechas.desdeBdHora(cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))),
            montoCuota = cursor.getInt(cursor.getColumnIndexOrThrow("monto")),
            montoPagado = cursor.getInt(cursor.getColumnIndexOrThrow("monto_pagado")),
            canal = Canal.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("canal"))),
            medioPago = cursor.getString(cursor.getColumnIndexOrThrow("medio_pago")),
            aTiempo = cursor.getInt(cursor.getColumnIndexOrThrow("a_tiempo")) == 1,
            nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            rutUsuario = cursor.getString(cursor.getColumnIndexOrThrow("rut"))
        )
    }
}
