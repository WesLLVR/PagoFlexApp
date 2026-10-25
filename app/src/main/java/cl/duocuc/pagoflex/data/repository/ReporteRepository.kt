package cl.duocuc.pagoflex.data.repository

import android.content.ContentValues
import android.database.Cursor
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_COMPROMISOS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_EMPRESAS
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_REPORTES
import cl.duocuc.pagoflex.data.PagoFlexDbHelper.Companion.TABLA_USUARIOS
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.util.Fechas
import java.util.Locale

class ReporteRepository(private val db: PagoFlexDbHelper) {

    companion object {
        private const val SQL_SELECT =
            "SELECT r.*, c.folio, c.concepto, e.nombre_fantasia, u.nombre AS nombre_usuario, " +
                "a.nombre AS nombre_agente FROM $TABLA_REPORTES r " +
                "INNER JOIN $TABLA_COMPROMISOS c ON r.id_compromiso = c.id_compromiso " +
                "INNER JOIN $TABLA_EMPRESAS e ON c.id_empresa = e.id_empresa " +
                "INNER JOIN $TABLA_USUARIOS u ON c.id_usuario = u.id_usuario " +
                "LEFT JOIN $TABLA_USUARIOS a ON r.id_agente = a.id_usuario"
    }

    fun crear(idCompromiso: Long, motivo: MotivoReporte, comentario: String, foto: ByteArray?): Long {
        val baseDatos = db.writableDatabase
        baseDatos.beginTransaction()
        try {
            val valores = ContentValues().apply {
                put("numero", siguienteNumero())
                put("id_compromiso", idCompromiso)
                put("motivo", motivo.name)
                put("comentario", comentario.trim())
                put("foto", foto)
                put("fecha", Fechas.aBd(Fechas.hoy()))
                put("estado", EstadoReporte.ABIERTO.name)
            }
            val id = baseDatos.insert(TABLA_REPORTES, null, valores)
            val estado = ContentValues().apply { put("estado", EstadoCompromiso.EN_REVISION.name) }
            baseDatos.update(TABLA_COMPROMISOS, estado, "id_compromiso = ?", arrayOf(idCompromiso.toString()))
            baseDatos.setTransactionSuccessful()
            return id
        } finally {
            baseDatos.endTransaction()
        }
    }

    fun ultimoDeCompromiso(idCompromiso: Long): Reporte? =
        consultar(
            "$SQL_SELECT WHERE r.id_compromiso = ? ORDER BY r.id_reporte DESC",
            arrayOf(idCompromiso.toString())
        ).firstOrNull()

    fun listar(): List<Reporte> =
        consultar("$SQL_SELECT ORDER BY r.fecha", null)

    fun buscarPorId(id: Long): Reporte? =
        consultar("$SQL_SELECT WHERE r.id_reporte = ?", arrayOf(id.toString())).firstOrNull()

    fun tomar(idReporte: Long, idAgente: Long): Int {
        val valores = ContentValues().apply {
            put("estado", EstadoReporte.EN_GESTION.name)
            put("id_agente", idAgente)
            put("fecha_toma", Fechas.aBd(Fechas.hoy()))
        }
        return db.writableDatabase.update(TABLA_REPORTES, valores, "id_reporte = ?", arrayOf(idReporte.toString()))
    }

    fun resolver(reporte: Reporte, respuesta: String) {
        val baseDatos = db.writableDatabase
        baseDatos.beginTransaction()
        try {
            val valores = ContentValues().apply {
                put("estado", EstadoReporte.RESUELTO.name)
                put("respuesta", respuesta.trim())
                put("fecha_resolucion", Fechas.aBd(Fechas.hoy()))
            }
            baseDatos.update(TABLA_REPORTES, valores, "id_reporte = ?", arrayOf(reporte.id.toString()))
            val estado = ContentValues().apply { put("estado", EstadoCompromiso.PENDIENTE.name) }
            baseDatos.update(
                TABLA_COMPROMISOS, estado, "id_compromiso = ? AND estado = ?",
                arrayOf(reporte.idCompromiso.toString(), EstadoCompromiso.EN_REVISION.name)
            )
            baseDatos.setTransactionSuccessful()
        } finally {
            baseDatos.endTransaction()
        }
    }

    private fun siguienteNumero(): String {
        val ultimo = db.readableDatabase.rawQuery(
            "SELECT MAX(numero) FROM $TABLA_REPORTES", null
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getString(0) else "REP-0000"
        }
        val numero = ultimo.removePrefix("REP-").toInt() + 1
        return String.format(Locale.ROOT, "REP-%04d", numero)
    }

    private fun consultar(sql: String, args: Array<String>?): List<Reporte> {
        val lista = mutableListOf<Reporte>()
        db.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        return lista
    }

    private fun leer(cursor: Cursor): Reporte {
        val columnaFoto = cursor.getColumnIndexOrThrow("foto")
        val columnaAgente = cursor.getColumnIndexOrThrow("nombre_agente")
        val columnaRespuesta = cursor.getColumnIndexOrThrow("respuesta")
        val columnaResolucion = cursor.getColumnIndexOrThrow("fecha_resolucion")
        return Reporte(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id_reporte")),
            numero = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
            idCompromiso = cursor.getLong(cursor.getColumnIndexOrThrow("id_compromiso")),
            folio = cursor.getString(cursor.getColumnIndexOrThrow("folio")),
            empresa = cursor.getString(cursor.getColumnIndexOrThrow("nombre_fantasia")),
            concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
            nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nombre_usuario")),
            motivo = MotivoReporte.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("motivo"))),
            comentario = cursor.getString(cursor.getColumnIndexOrThrow("comentario")),
            foto = if (cursor.isNull(columnaFoto)) null else cursor.getBlob(columnaFoto),
            fecha = Fechas.desdeBd(cursor.getString(cursor.getColumnIndexOrThrow("fecha"))),
            estado = EstadoReporte.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("estado"))),
            nombreAgente = if (cursor.isNull(columnaAgente)) null else cursor.getString(columnaAgente),
            respuesta = if (cursor.isNull(columnaRespuesta)) null else cursor.getString(columnaRespuesta),
            fechaResolucion = if (cursor.isNull(columnaResolucion)) null
                else Fechas.desdeBd(cursor.getString(columnaResolucion))
        )
    }
}
