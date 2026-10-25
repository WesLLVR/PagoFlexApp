package cl.duocuc.pagoflex.data.repository

import android.content.ContentValues
import android.database.Cursor
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_COMPROMISOS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_EMPRESAS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_PAGOS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_USUARIOS
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.Empresa
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.ResumenCobranza
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class CompromisoRepository(private val db: PagoFlexDbHelper) {

    companion object {
        private const val SQL_SELECT =
            "SELECT c.*, u.nombre AS nombre_usuario, u.rut AS rut_usuario, " +
                "e.nombre_fantasia, e.recargo FROM $TABLA_COMPROMISOS c " +
                "INNER JOIN $TABLA_USUARIOS u ON c.id_usuario = u.id_usuario " +
                "INNER JOIN $TABLA_EMPRESAS e ON c.id_empresa = e.id_empresa"
    }

    fun listarDeUsuario(idUsuario: Long): List<Compromiso> =
        consultar("$SQL_SELECT WHERE c.id_usuario = ? ORDER BY c.fecha_vencimiento", arrayOf(idUsuario.toString()))

    fun listarDeEmpresa(idEmpresa: Long): List<Compromiso> =
        consultar("$SQL_SELECT WHERE c.id_empresa = ? ORDER BY c.fecha_vencimiento DESC", arrayOf(idEmpresa.toString()))

    fun buscarPorId(id: Long): Compromiso? =
        consultar("$SQL_SELECT WHERE c.id_compromiso = ?", arrayOf(id.toString())).firstOrNull()

    fun buscarEmpresa(idEmpresa: Long): Empresa? =
        db.readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_EMPRESAS WHERE id_empresa = ?", arrayOf(idEmpresa.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) null
            else Empresa(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id_empresa")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre_fantasia")),
                recargo = cursor.getInt(cursor.getColumnIndexOrThrow("recargo"))
            )
        }

    fun existeDuplicado(idUsuario: Long, idEmpresa: Long, concepto: String, numeroCuota: Int?): Boolean {
        val sql = "SELECT COUNT(*) FROM $TABLA_COMPROMISOS WHERE id_usuario = ? AND id_empresa = ? " +
            "AND LOWER(concepto) = LOWER(?) AND IFNULL(numero_cuota, 0) = CAST(? AS INTEGER) AND estado != ?"
        val args = arrayOf(
            idUsuario.toString(), idEmpresa.toString(), concepto.trim(),
            (numeroCuota ?: 0).toString(), EstadoCompromiso.ANULADO.name
        )
        return db.readableDatabase.rawQuery(sql, args).use { cursor ->
            cursor.moveToFirst() && cursor.getInt(0) > 0
        }
    }

    fun registrar(
        idUsuario: Long, idEmpresa: Long, concepto: String, numeroCuota: Int?, totalCuotas: Int?,
        monto: Int, vencimiento: LocalDate, idEjecutivo: Long
    ): Long {
        val valores = ContentValues().apply {
            put("folio", siguienteFolio())
            put("id_usuario", idUsuario)
            put("id_empresa", idEmpresa)
            put("concepto", concepto.trim())
            put("numero_cuota", numeroCuota)
            put("total_cuotas", totalCuotas)
            put("monto", monto)
            put("fecha_vencimiento", Fechas.aBd(vencimiento))
            put("estado", EstadoCompromiso.PENDIENTE.name)
            put("id_ejecutivo_registro", idEjecutivo)
            put("fecha_registro", Fechas.aBd(Fechas.hoy()))
        }
        return db.writableDatabase.insert(TABLA_COMPROMISOS, null, valores)
    }

    fun anular(idCompromiso: Long, motivo: MotivoAnulacion, comentario: String, idEjecutivo: Long): Int {
        val valores = ContentValues().apply {
            put("estado", EstadoCompromiso.ANULADO.name)
            put("motivo_anulacion", motivo.name)
            put("comentario_anulacion", comentario.trim())
            put("id_ejecutivo_anulacion", idEjecutivo)
            put("fecha_anulacion", Fechas.aBd(Fechas.hoy()))
        }
        return db.writableDatabase.update(TABLA_COMPROMISOS, valores, "id_compromiso = ?", arrayOf(idCompromiso.toString()))
    }

    fun cambiarEstado(idCompromiso: Long, estado: EstadoCompromiso): Int {
        val valores = ContentValues().apply { put("estado", estado.name) }
        return db.writableDatabase.update(TABLA_COMPROMISOS, valores, "id_compromiso = ?", arrayOf(idCompromiso.toString()))
    }

    fun cumplimiento(idUsuario: Long): Cumplimiento {
        val filas = filasCumplimiento(idUsuario)
        return Cumplimiento(considerados = filas.size, aTiempo = filas.count { it.second })
    }

    private fun filasCumplimiento(idUsuario: Long): List<Pair<LocalDate, Boolean>> {
        val hoy = Fechas.hoy()
        val sql = "SELECT c.fecha_vencimiento, IFNULL(p.a_tiempo, 0) AS a_tiempo FROM $TABLA_COMPROMISOS c " +
            "LEFT JOIN $TABLA_PAGOS p ON p.id_compromiso = c.id_compromiso " +
            "WHERE c.id_usuario = ? AND c.fecha_vencimiento >= ? AND c.fecha_vencimiento < ? " +
            "AND c.estado NOT IN (?, ?)"
        val args = arrayOf(
            idUsuario.toString(), Fechas.aBd(hoy.minusMonths(12)), Fechas.aBd(hoy),
            EstadoCompromiso.ANULADO.name, EstadoCompromiso.EN_REVISION.name
        )
        val filas = mutableListOf<Pair<LocalDate, Boolean>>()
        db.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                filas.add(Fechas.desdeBd(cursor.getString(0)) to (cursor.getInt(1) == 1))
            }
        }
        return filas
    }

    fun resumenCobranza(idEmpresa: Long, mes: YearMonth): ResumenCobranza {
        val delMes = listarDeEmpresa(idEmpresa).filter { YearMonth.from(it.vencimiento) == mes }
        val sql = "SELECT IFNULL(SUM(p.monto_pagado), 0) FROM $TABLA_PAGOS p " +
            "INNER JOIN $TABLA_COMPROMISOS c ON p.id_compromiso = c.id_compromiso " +
            "WHERE c.id_empresa = ? AND c.fecha_vencimiento LIKE ?"
        val patron = String.format(Locale.ROOT, "%04d-%02d%%", mes.year, mes.monthValue)
        val recaudado = db.readableDatabase.rawQuery(sql, arrayOf(idEmpresa.toString(), patron)).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        return ResumenCobranza(
            pagados = delMes.count { it.estado == EstadoCompromiso.PAGADO },
            pendientes = delMes.count { it.estado == EstadoCompromiso.PENDIENTE },
            vencidos = delMes.count { it.estado == EstadoCompromiso.VENCIDO },
            enRevision = delMes.count { it.estado == EstadoCompromiso.EN_REVISION },
            anulados = delMes.count { it.estado == EstadoCompromiso.ANULADO },
            montoRecaudado = recaudado
        )
    }

    private fun siguienteFolio(): String {
        val siguiente = db.readableDatabase.rawQuery(
            "SELECT IFNULL(MAX(id_compromiso), 0) + 1 FROM $TABLA_COMPROMISOS", null
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        return String.format(Locale.ROOT, "CP-%d-%04d", Fechas.hoy().year, 300 + siguiente)
    }

    private fun consultar(sql: String, args: Array<String>?): List<Compromiso> {
        val lista = mutableListOf<Compromiso>()
        db.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        return lista
    }

    private fun leer(cursor: Cursor): Compromiso {
        val columnaCuota = cursor.getColumnIndexOrThrow("numero_cuota")
        val columnaTotal = cursor.getColumnIndexOrThrow("total_cuotas")
        val columnaMotivo = cursor.getColumnIndexOrThrow("motivo_anulacion")
        return Compromiso(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id_compromiso")),
            folio = cursor.getString(cursor.getColumnIndexOrThrow("folio")),
            idUsuario = cursor.getLong(cursor.getColumnIndexOrThrow("id_usuario")),
            nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nombre_usuario")),
            rutUsuario = cursor.getString(cursor.getColumnIndexOrThrow("rut_usuario")),
            idEmpresa = cursor.getLong(cursor.getColumnIndexOrThrow("id_empresa")),
            empresa = cursor.getString(cursor.getColumnIndexOrThrow("nombre_fantasia")),
            concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
            numeroCuota = if (cursor.isNull(columnaCuota)) null else cursor.getInt(columnaCuota),
            totalCuotas = if (cursor.isNull(columnaTotal)) null else cursor.getInt(columnaTotal),
            monto = cursor.getInt(cursor.getColumnIndexOrThrow("monto")),
            vencimiento = Fechas.desdeBd(cursor.getString(cursor.getColumnIndexOrThrow("fecha_vencimiento"))),
            estadoGuardado = EstadoCompromiso.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("estado"))),
            recargoEmpresa = cursor.getInt(cursor.getColumnIndexOrThrow("recargo")),
            motivoAnulacion = if (cursor.isNull(columnaMotivo)) null
                else MotivoAnulacion.valueOf(cursor.getString(columnaMotivo)).etiqueta
        )
    }
}
