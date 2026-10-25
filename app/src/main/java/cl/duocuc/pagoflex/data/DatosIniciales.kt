package cl.duocuc.pagoflex.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import cl.duocuc.pagoflex.model.Canal
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.EstadoReporte
import cl.duocuc.pagoflex.model.MedioPago
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.model.Rol
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object DatosIniciales {

    const val ID_CAMILA = 1L
    const val ID_ANDRES = 9L
    const val ID_PAULA = 14L

    private val hoy: LocalDate get() = Fechas.hoy()
    private var contadorFolio = 1
    private var contadorComprobante = 300

    fun cargar(db: SQLiteDatabase) {
        contadorFolio = 1
        contadorComprobante = 300
        cargarEmpresas(db)
        cargarUsuarios(db)
        cargarCompromisosCamila(db)
        cargarCompromisosAltosDelMaipo(db)
        cargarOtrosCompromisos(db)
    }

    private fun cargarEmpresas(db: SQLiteDatabase) {
        empresa(db, 1, "EC-01", "Altos del Maipo", "Inmobiliaria Altos del Maipo SpA", "76.845.213-K", "Inmobiliaria", "atencion@altosmaipo.ejemplo", "+56 2 0000 0201", 8500)
        empresa(db, 2, "EC-02", "Colegio Bosque Nativo", "Corporación Educacional Bosque Nativo", "65.132.874-8", "Educación escolar", "pagos@bosquenativo.ejemplo", "+56 2 0000 0202", 5000)
        empresa(db, 3, "EC-03", "Crédito Andino", "Crédito Andino S.A.", "96.571.438-3", "Crédito de consumo", "clientes@creditoandino.ejemplo", "+56 2 0000 0203", 12000)
        empresa(db, 4, "EC-04", "Horizonte Sur", "Seguros Horizonte Sur S.A.", "96.814.205-4", "Seguros", "contacto@horizontesur.ejemplo", "+56 2 0000 0204", 3000)
        empresa(db, 5, "EC-05", "Manos Abiertas", "Fundación Manos Abiertas", "65.247.190-0", "ONG", "socios@manosabiertas.ejemplo", "+56 2 0000 0205", 0)
        empresa(db, 6, "EC-06", "Club Los Halcones", "Club Deportivo Los Halcones", "65.398.412-K", "Club deportivo", "tesoreria@loshalcones.ejemplo", "+56 2 0000 0206", 2000)
        empresa(db, 7, "EC-07", "Instituto Cordillera", "Instituto Técnico Cordillera SpA", "77.214.583-7", "Educación superior", "finanzas@itcordillera.ejemplo", "+56 2 0000 0207", 6000)
        empresa(db, 8, "EC-08", "Plaza Oriente", "Arriendos Plaza Oriente Ltda.", "76.432.871-K", "Administración de arriendos", "cobranza@plazaoriente.ejemplo", "+56 2 0000 0208", 7500)
    }

    private fun cargarUsuarios(db: SQLiteDatabase) {
        val final = Rol.USUARIO_FINAL
        usuario(db, 1, "UF-01", "Camila Andrea Rojas Peña", "15.834.207-3", "crojas@correo.ejemplo", "+56 9 0000 0101", "Maipú", final)
        usuario(db, 2, "UF-02", "Luis Alberto Muñoz Tapia", "12.476.318-5", "lmunoz@correo.ejemplo", "+56 9 0000 0102", "Puente Alto", final)
        usuario(db, 3, "UF-03", "Rosa Elena Contreras Díaz", "6.893.541-5", "rcontreras@correo.ejemplo", "+56 9 0000 0103", "San Miguel", final)
        usuario(db, 4, "UF-04", "Matías Ignacio Soto Vergara", "21.045.873-5", "msoto@correo.ejemplo", "+56 9 0000 0104", "La Florida", final)
        usuario(db, 5, "UF-05", "Fernanda Paz Godoy Lagos", "17.562.904-1", "fgodoy@correo.ejemplo", "+56 9 0000 0105", "Ñuñoa", final)
        usuario(db, 6, "UF-06", "Héctor Manuel Pizarro Campos", "10.318.265-4", "hpizarro@correo.ejemplo", "+56 9 0000 0106", "Quilicura", final)
        usuario(db, 7, "UF-07", "Javiera Constanza Reyes Molina", "19.287.430-0", "jreyes@correo.ejemplo", "+56 9 0000 0107", "San Bernardo", final)
        usuario(db, 8, "UF-08", "Benjamín Esteban Araya Fuentes", "18.704.396-4", "baraya@correo.ejemplo", "+56 9 0000 0108", "Estación Central", final)

        val agente = Rol.AGENTE
        usuario(db, 9, "AG-01", "Andrés Felipe Lagos Meza", "16.248.317-K", "alagos@pagoflex.ejemplo", null, null, agente, cargo = "Soporte")
        usuario(db, 10, "AG-02", "Daniela Sofía Ortiz Bravo", "18.352.091-1", "dortiz@pagoflex.ejemplo", null, null, agente, cargo = "Soporte")
        usuario(db, 11, "AG-03", "Ricardo José Salinas Cáceres", "13.907.264-2", "rsalinas@pagoflex.ejemplo", null, null, agente, cargo = "Soporte")
        usuario(db, 12, "AG-04", "Valentina Ignacia Paredes Silva", "17.781.435-0", "vparedes@pagoflex.ejemplo", null, null, agente, cargo = "Comercial")
        usuario(db, 13, "AG-05", "Tomás Alejandro Vidal Rojas", "19.564.028-9", "tvidal@pagoflex.ejemplo", null, null, agente, cargo = "Comercial")

        val ejecutivo = Rol.EJECUTIVO
        usuario(db, 14, "EJ-01", "Paula Andrea Saavedra Núñez", "14.629.853-2", "psaavedra@altosmaipo.ejemplo", "+56 9 0000 0401", null, ejecutivo, 1, "Ejecutiva de cobranza")
        usuario(db, 15, "EJ-02", "Marco Antonio Fuentes Riquelme", "11.853.720-3", "mfuentes@bosquenativo.ejemplo", "+56 9 0000 0402", null, ejecutivo, 2, "Encargado de finanzas")
        usuario(db, 16, "EJ-03", "Claudia Beatriz Henríquez Soto", "15.370.946-7", "chenriquez@creditoandino.ejemplo", "+56 9 0000 0403", null, ejecutivo, 3, "Jefa de cobranza")
        usuario(db, 17, "EJ-04", "Felipe Ignacio Morales Tapia", "17.095.482-3", "fmorales@creditoandino.ejemplo", "+56 9 0000 0404", null, ejecutivo, 3, "Ejecutivo de cobranza")
        usuario(db, 18, "EJ-05", "Gloria Inés Carrasco Vega", "9.846.217-1", "gcarrasco@loshalcones.ejemplo", "+56 9 0000 0405", null, ejecutivo, 6, "Tesorera")
        usuario(db, 19, "EJ-06", "Sebastián Andrés Núñez Olivares", "18.930.571-0", "snunez@itcordillera.ejemplo", "+56 9 0000 0406", null, ejecutivo, 7, "Analista de finanzas")
        usuario(db, 20, "EJ-07", "Mónica Patricia Leiva Castro", "12.584.396-4", "mleiva@plazaoriente.ejemplo", "+56 9 0000 0407", null, ejecutivo, 8, "Administradora de cobranza")
    }

    private fun cargarCompromisosCamila(db: SQLiteDatabase) {
        val canales = listOf(Canal.PORTAL, Canal.AGENTE, Canal.APP)
        val medios = MedioPago.entries
        for (mes in 1..11) {
            val canal = canales[mes % canales.size]
            val medio = medios[mes % medios.size].etiqueta
            val cuota = (9 - mes + 12) % 12 + 1

            val vencArriendo = hoy.minusMonths(mes.toLong()).plusDays(4)
            val arriendo = compromiso(db, folioHistorico(vencArriendo), 1, 8, "Arriendo departamento ${nombreMes(vencArriendo)}", cuota, 12, 520000, vencArriendo, EstadoCompromiso.PAGADO, 20)
            pago(db, arriendo, vencArriendo.minusDays(1), "13:05", 520000, canal, medio, true)

            val vencClub = hoy.minusMonths(mes.toLong()).minusDays(1)
            val club = compromiso(db, folioHistorico(vencClub), 1, 6, "Cuota social ${nombreMes(vencClub)}", cuota, 12, 15000, vencClub, EstadoCompromiso.PAGADO, 18)
            if (mes == 2 || mes == 5) {
                pago(db, club, vencClub.plusDays(2), "18:40", 17000, canal, medio, false)
            } else {
                pago(db, club, vencClub, "09:15", 15000, canal, medio, true)
            }

            val vencAporte = hoy.minusMonths(mes.toLong()).plusDays(9)
            val aporte = compromiso(db, folioHistorico(vencAporte), 1, 5, "Aporte mensual ${nombreMes(vencAporte)}", null, null, 10000, vencAporte, EstadoCompromiso.PAGADO, null)
            pago(db, aporte, vencAporte.minusDays(3), "08:22", 10000, canal, medio, true)
        }

        val vencCredito6 = hoy.minusDays(56)
        val credito6 = compromiso(db, "CP-2026-0098", 1, 3, "Crédito de consumo", 6, 24, 86400, vencCredito6, EstadoCompromiso.PAGADO, 16)
        pago(db, credito6, vencCredito6.minusDays(1), "11:30", 86400, Canal.PORTAL, MedioPago.DEBITO.etiqueta, true)

        val vencCredito7 = hoy.minusDays(26)
        val credito7 = compromiso(db, "CP-2026-0104", 1, 3, "Crédito de consumo", 7, 24, 86400, vencCredito7, EstadoCompromiso.PAGADO, 16)
        pago(db, credito7, vencCredito7.minusDays(1), "10:42", 86400, Canal.PORTAL, MedioPago.DEBITO.etiqueta, true, "CPR-000451")

        val vencArriendo = hoy.plusDays(4)
        compromiso(db, "CP-2026-0101", 1, 8, "Arriendo departamento ${nombreMes(vencArriendo)}", 10, 12, 520000, vencArriendo, EstadoCompromiso.PENDIENTE, 20)
        val vencClub = hoy.minusDays(1)
        compromiso(db, "CP-2026-0102", 1, 6, "Cuota social ${nombreMes(vencClub)}", 10, 12, 15000, vencClub, EstadoCompromiso.PENDIENTE, 18)
        val vencAporte = hoy.plusDays(9)
        compromiso(db, "CP-2026-0103", 1, 5, "Aporte mensual ${nombreMes(vencAporte)}", null, null, 10000, vencAporte, EstadoCompromiso.PENDIENTE, null)
        compromiso(db, "CP-2026-0105", 1, 3, "Crédito de consumo", 8, 24, 86400, hoy.plusDays(19), EstadoCompromiso.PENDIENTE, 16)
        val vencClubSiguiente = hoy.plusDays(29)
        compromiso(db, "CP-2026-0113", 1, 6, "Cuota social ${nombreMes(vencClubSiguiente)}", 11, 12, 15000, vencClubSiguiente, EstadoCompromiso.PENDIENTE, 18)
    }

    private fun cargarCompromisosAltosDelMaipo(db: SQLiteDatabase) {
        val cuota13 = compromiso(db, "CP-2026-0072", 2, 1, "Cuota pie departamento", 13, 36, 310000, hoy.minusDays(36), EstadoCompromiso.PAGADO, 14)
        pago(db, cuota13, hoy.minusDays(34), "21:14", 318500, Canal.AGENTE, MedioPago.TRANSFERENCIA.etiqueta, false, "CPR-000377")
        val cuota14 = compromiso(db, "CP-2026-0106", 2, 1, "Cuota pie departamento", 14, 36, 310000, hoy.minusDays(6), EstadoCompromiso.EN_REVISION, 14)
        compromiso(db, "CP-2026-0107", 2, 1, "Cuota pie departamento", 15, 36, 310000, hoy.plusDays(24), EstadoCompromiso.PENDIENTE, 14)
        reporte(db, "REP-0012", cuota14, MotivoReporte.MONTO_INCORRECTO, "No sé por qué me cobran $ 318.500.", hoy.minusDays(5), EstadoReporte.ABIERTO, null, null)

        val arrendatarios = listOf(5L to 5, 6L to 15, 8L to 25)
        var numero = 1
        for ((idUsuario, dia) in arrendatarios) {
            for (mes in 0..2) {
                val base = hoy.minusMonths(mes.toLong())
                val venc = base.withDayOfMonth(dia.coerceAtMost(base.lengthOfMonth()))
                val folio = String.format(Locale.ROOT, "CP-%d-02%02d", venc.year, numero++)
                val cuota = 20 - mes
                when {
                    idUsuario == 8L && mes == 1 -> {
                        val id = compromiso(db, folio, idUsuario, 1, "Cuota pie departamento", cuota, 36, 290000, venc, EstadoCompromiso.ANULADO, 14)
                        anular(db, id, MotivoAnulacion.REPACTACION, "", 14, venc.minusDays(2))
                    }
                    !venc.isBefore(hoy) -> compromiso(db, folio, idUsuario, 1, "Cuota pie departamento", cuota, 36, 290000, venc, EstadoCompromiso.PENDIENTE, 14)
                    idUsuario == 6L && mes == 0 -> compromiso(db, folio, idUsuario, 1, "Cuota pie departamento", cuota, 36, 290000, venc, EstadoCompromiso.PENDIENTE, 14)
                    idUsuario == 5L && mes == 1 -> {
                        val id = compromiso(db, folio, idUsuario, 1, "Cuota pie departamento", cuota, 36, 290000, venc, EstadoCompromiso.PAGADO, 14)
                        pago(db, id, venc.plusDays(3), "19:20", 298500, Canal.PORTAL, MedioPago.CREDITO.etiqueta, false)
                    }
                    else -> {
                        val id = compromiso(db, folio, idUsuario, 1, "Cuota pie departamento", cuota, 36, 290000, venc, EstadoCompromiso.PAGADO, 14)
                        pago(db, id, venc.minusDays(1), "12:10", 290000, Canal.APP, MedioPago.DEBITO.etiqueta, true)
                    }
                }
            }
        }
    }

    private fun cargarOtrosCompromisos(db: SQLiteDatabase) {
        val seguro = compromiso(db, "CP-2026-0108", 3, 4, "Seguro de hogar", 10, 12, 18900, hoy.minusDays(23), EstadoCompromiso.PAGADO, null)
        pago(db, seguro, hoy.minusDays(24), "19:05", 18900, Canal.AGENTE, MedioPago.DEBITO.etiqueta, true, "CPR-000452")

        val arancel = compromiso(db, "CP-2026-0109", 4, 7, "Arancel", 8, 10, 142000, hoy.minusDays(19), EstadoCompromiso.EN_REVISION, 19)
        reporte(db, "REP-0011", arancel, MotivoReporte.YA_PAGUE, "Pagué por transferencia directa al instituto.", hoy.minusDays(17), EstadoReporte.EN_GESTION, 10, null)

        compromiso(db, "CP-2026-0110", 5, 2, "Mensualidad", 8, 10, 185000, hoy.plusDays(4), EstadoCompromiso.PENDIENTE, 15)

        val credito = compromiso(db, "CP-2026-0111", 6, 3, "Crédito de consumo", 19, 36, 54200, hoy.minusDays(34), EstadoCompromiso.ANULADO, 17)
        anular(db, credito, MotivoAnulacion.REPACTACION, "", 17, hoy.minusDays(32))
        reporte(db, "REP-0009", credito, MotivoReporte.MONTO_INCORRECTO, "Repacté el crédito y me siguen cobrando la cuota antigua.", hoy.minusDays(36), EstadoReporte.RESUELTO, 9, "La empresa anuló la cuota por repactación.")

        val cuotaClub = compromiso(db, "CP-2026-0112", 7, 6, "Cuota social", 10, 12, 15000, hoy.minusDays(1), EstadoCompromiso.PAGADO, 18)
        pago(db, cuotaClub, hoy.minusDays(2), "08:15", 15000, Canal.APP, MedioPago.DEBITO.etiqueta, true, "CPR-000453")
    }

    private fun nombreMes(fecha: LocalDate): String =
        fecha.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CL"))

    private fun folioHistorico(fecha: LocalDate): String =
        String.format(Locale.ROOT, "CP-%d-%04d", fecha.year, contadorFolio++)

    private fun empresa(
        db: SQLiteDatabase, id: Long, codigo: String, nombre: String, razonSocial: String,
        rut: String, rubro: String, correo: String, telefono: String, recargo: Int
    ) {
        val valores = ContentValues().apply {
            put("id_empresa", id)
            put("codigo", codigo)
            put("nombre_fantasia", nombre)
            put("razon_social", razonSocial)
            put("rut", rut)
            put("rubro", rubro)
            put("correo", correo)
            put("telefono", telefono)
            put("recargo", recargo)
        }
        db.insert(PagoFlexDbHelper.TABLA_EMPRESAS, null, valores)
    }

    private fun usuario(
        db: SQLiteDatabase, id: Long, codigo: String, nombre: String, rut: String, correo: String,
        telefono: String?, comuna: String?, rol: Rol, idEmpresa: Long? = null, cargo: String? = null
    ) {
        val valores = ContentValues().apply {
            put("id_usuario", id)
            put("codigo", codigo)
            put("nombre", nombre)
            put("rut", rut)
            put("correo", correo)
            put("telefono", telefono)
            put("comuna", comuna)
            put("rol", rol.name)
            put("id_empresa", idEmpresa)
            put("cargo", cargo)
        }
        db.insert(PagoFlexDbHelper.TABLA_USUARIOS, null, valores)
    }

    private fun compromiso(
        db: SQLiteDatabase, folio: String, idUsuario: Long, idEmpresa: Long, concepto: String,
        numeroCuota: Int?, totalCuotas: Int?, monto: Int, vencimiento: LocalDate,
        estado: EstadoCompromiso, idEjecutivo: Long?
    ): Long {
        val valores = ContentValues().apply {
            put("folio", folio)
            put("id_usuario", idUsuario)
            put("id_empresa", idEmpresa)
            put("concepto", concepto)
            put("numero_cuota", numeroCuota)
            put("total_cuotas", totalCuotas)
            put("monto", monto)
            put("fecha_vencimiento", Fechas.aBd(vencimiento))
            put("estado", estado.name)
            put("id_ejecutivo_registro", idEjecutivo)
            put("fecha_registro", Fechas.aBd(vencimiento.minusDays(25)))
        }
        return db.insert(PagoFlexDbHelper.TABLA_COMPROMISOS, null, valores)
    }

    private fun pago(
        db: SQLiteDatabase, idCompromiso: Long, fecha: LocalDate, hora: String, monto: Int,
        canal: Canal, medio: String, aTiempo: Boolean, numero: String? = null
    ) {
        val valores = ContentValues().apply {
            put("numero_comprobante", numero ?: String.format(Locale.ROOT, "CPR-%06d", contadorComprobante++))
            put("id_compromiso", idCompromiso)
            put("fecha_hora", "${Fechas.aBd(fecha)} $hora")
            put("monto_pagado", monto)
            put("canal", canal.name)
            put("medio_pago", medio)
            put("a_tiempo", if (aTiempo) 1 else 0)
        }
        db.insert(PagoFlexDbHelper.TABLA_PAGOS, null, valores)
    }

    private fun anular(
        db: SQLiteDatabase, idCompromiso: Long, motivo: MotivoAnulacion, comentario: String,
        idEjecutivo: Long, fecha: LocalDate
    ) {
        val valores = ContentValues().apply {
            put("motivo_anulacion", motivo.name)
            put("comentario_anulacion", comentario)
            put("id_ejecutivo_anulacion", idEjecutivo)
            put("fecha_anulacion", Fechas.aBd(fecha))
        }
        db.update(PagoFlexDbHelper.TABLA_COMPROMISOS, valores, "id_compromiso = ?", arrayOf(idCompromiso.toString()))
    }

    private fun reporte(
        db: SQLiteDatabase, numero: String, idCompromiso: Long, motivo: MotivoReporte, comentario: String,
        fecha: LocalDate, estado: EstadoReporte, idAgente: Long?, respuesta: String?
    ) {
        val valores = ContentValues().apply {
            put("numero", numero)
            put("id_compromiso", idCompromiso)
            put("motivo", motivo.name)
            put("comentario", comentario)
            put("fecha", Fechas.aBd(fecha))
            put("estado", estado.name)
            put("id_agente", idAgente)
            if (idAgente != null) put("fecha_toma", Fechas.aBd(fecha.plusDays(1)))
            put("respuesta", respuesta)
            if (estado == EstadoReporte.RESUELTO) put("fecha_resolucion", Fechas.aBd(fecha.plusDays(4)))
        }
        db.insert(PagoFlexDbHelper.TABLA_REPORTES, null, valores)
    }
}
