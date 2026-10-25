package cl.duocuc.pagoflex.data.repository

import android.content.ContentValues
import android.database.Cursor
import cl.duocuc.pagoflex.data.DatosIniciales
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.model.Usuario
import java.util.Locale

class UsuarioRepository(private val db: PagoFlexDbHelper) {

    companion object {
        private const val SQL_SELECT =
            "SELECT u.*, e.nombre_fantasia FROM ${PagoFlexDbHelper.TABLA_USUARIOS} u " +
                "LEFT JOIN ${PagoFlexDbHelper.TABLA_EMPRESAS} e ON u.id_empresa = e.id_empresa"
    }

    fun sesionesDisponibles(): List<Usuario> =
        listOfNotNull(
            buscarPorId(DatosIniciales.ID_CAMILA),
            buscarPorId(DatosIniciales.ID_ANDRES),
            buscarPorId(DatosIniciales.ID_PAULA)
        )

    fun buscarPorId(id: Long): Usuario? =
        consultar("$SQL_SELECT WHERE u.id_usuario = ?", arrayOf(id.toString())).firstOrNull()

    fun buscarUsuarioFinalPorRut(rut: String): Usuario? =
        consultar(
            "$SQL_SELECT WHERE REPLACE(u.rut, '.', '') = ? AND u.rol = ?",
            arrayOf(rut.replace(".", "").uppercase(), Rol.USUARIO_FINAL.name)
        ).firstOrNull()

    fun registrarUsuarioFinal(nombre: String, rut: String, correo: String, telefono: String): Long {
        val cantidad = db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${PagoFlexDbHelper.TABLA_USUARIOS} WHERE rol = ?",
            arrayOf(Rol.USUARIO_FINAL.name)
        ).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        val valores = ContentValues().apply {
            put("codigo", String.format(Locale.ROOT, "UF-%02d", cantidad + 1))
            put("nombre", nombre.trim())
            put("rut", rut)
            put("correo", correo.trim())
            put("telefono", telefono.trim())
            put("rol", Rol.USUARIO_FINAL.name)
        }
        return db.writableDatabase.insert(PagoFlexDbHelper.TABLA_USUARIOS, null, valores)
    }

    private fun consultar(sql: String, args: Array<String>): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        db.readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        return lista
    }

    private fun leer(cursor: Cursor): Usuario {
        val columnaEmpresa = cursor.getColumnIndexOrThrow("id_empresa")
        val columnaCargo = cursor.getColumnIndexOrThrow("cargo")
        val columnaNombreEmpresa = cursor.getColumnIndexOrThrow("nombre_fantasia")
        return Usuario(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id_usuario")),
            codigo = cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            rut = cursor.getString(cursor.getColumnIndexOrThrow("rut")),
            correo = cursor.getString(cursor.getColumnIndexOrThrow("correo")),
            rol = Rol.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("rol"))),
            idEmpresa = if (cursor.isNull(columnaEmpresa)) null else cursor.getLong(columnaEmpresa),
            nombreEmpresa = if (cursor.isNull(columnaNombreEmpresa)) null else cursor.getString(columnaNombreEmpresa),
            cargo = if (cursor.isNull(columnaCargo)) null else cursor.getString(columnaCargo)
        )
    }
}
