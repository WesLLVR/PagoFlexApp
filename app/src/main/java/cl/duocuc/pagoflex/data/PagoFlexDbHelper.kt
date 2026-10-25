package cl.duocuc.pagoflex.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PagoFlexDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "pagoflex.db"
        private const val VERSION_BD = 1

        const val TABLA_EMPRESAS = "empresas"
        const val TABLA_USUARIOS = "usuarios"
        const val TABLA_COMPROMISOS = "compromisos"
        const val TABLA_PAGOS = "pagos"
        const val TABLA_REPORTES = "reportes"

        private const val SQL_CREAR_EMPRESAS =
            "CREATE TABLE $TABLA_EMPRESAS (" +
                "id_empresa INTEGER PRIMARY KEY, " +
                "codigo TEXT NOT NULL UNIQUE, " +
                "nombre_fantasia TEXT NOT NULL, " +
                "razon_social TEXT NOT NULL, " +
                "rut TEXT NOT NULL, " +
                "rubro TEXT NOT NULL, " +
                "correo TEXT NOT NULL, " +
                "telefono TEXT NOT NULL, " +
                "recargo INTEGER NOT NULL)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                "id_usuario INTEGER PRIMARY KEY, " +
                "codigo TEXT NOT NULL UNIQUE, " +
                "nombre TEXT NOT NULL, " +
                "rut TEXT NOT NULL UNIQUE, " +
                "correo TEXT NOT NULL, " +
                "telefono TEXT, " +
                "comuna TEXT, " +
                "rol TEXT NOT NULL, " +
                "id_empresa INTEGER REFERENCES $TABLA_EMPRESAS(id_empresa), " +
                "cargo TEXT)"

        private const val SQL_CREAR_COMPROMISOS =
            "CREATE TABLE $TABLA_COMPROMISOS (" +
                "id_compromiso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "folio TEXT NOT NULL UNIQUE, " +
                "id_usuario INTEGER NOT NULL REFERENCES $TABLA_USUARIOS(id_usuario), " +
                "id_empresa INTEGER NOT NULL REFERENCES $TABLA_EMPRESAS(id_empresa), " +
                "concepto TEXT NOT NULL, " +
                "numero_cuota INTEGER, " +
                "total_cuotas INTEGER, " +
                "monto INTEGER NOT NULL CHECK (monto > 0), " +
                "fecha_vencimiento TEXT NOT NULL, " +
                "estado TEXT NOT NULL, " +
                "id_ejecutivo_registro INTEGER REFERENCES $TABLA_USUARIOS(id_usuario), " +
                "fecha_registro TEXT NOT NULL, " +
                "motivo_anulacion TEXT, " +
                "comentario_anulacion TEXT, " +
                "id_ejecutivo_anulacion INTEGER REFERENCES $TABLA_USUARIOS(id_usuario), " +
                "fecha_anulacion TEXT)"

        private const val SQL_CREAR_PAGOS =
            "CREATE TABLE $TABLA_PAGOS (" +
                "id_pago INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero_comprobante TEXT NOT NULL UNIQUE, " +
                "id_compromiso INTEGER NOT NULL UNIQUE REFERENCES $TABLA_COMPROMISOS(id_compromiso), " +
                "fecha_hora TEXT NOT NULL, " +
                "monto_pagado INTEGER NOT NULL, " +
                "canal TEXT NOT NULL, " +
                "medio_pago TEXT NOT NULL, " +
                "a_tiempo INTEGER NOT NULL)"

        private const val SQL_CREAR_REPORTES =
            "CREATE TABLE $TABLA_REPORTES (" +
                "id_reporte INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero TEXT NOT NULL UNIQUE, " +
                "id_compromiso INTEGER NOT NULL REFERENCES $TABLA_COMPROMISOS(id_compromiso), " +
                "motivo TEXT NOT NULL, " +
                "comentario TEXT NOT NULL, " +
                "foto BLOB, " +
                "fecha TEXT NOT NULL, " +
                "estado TEXT NOT NULL, " +
                "id_agente INTEGER REFERENCES $TABLA_USUARIOS(id_usuario), " +
                "fecha_toma TEXT, " +
                "respuesta TEXT, " +
                "fecha_resolucion TEXT)"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_EMPRESAS)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_CREAR_COMPROMISOS)
        db.execSQL(SQL_CREAR_PAGOS)
        db.execSQL(SQL_CREAR_REPORTES)
        DatosIniciales.cargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_REPORTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PAGOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_COMPROMISOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_EMPRESAS")
        onCreate(db)
    }
}
