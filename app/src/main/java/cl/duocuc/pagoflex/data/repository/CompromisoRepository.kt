package cl.duocuc.pagoflex.data.repository

import cl.duocuc.pagoflex.data.remote.AnularRequest
import cl.duocuc.pagoflex.data.remote.CompromisoApi
import cl.duocuc.pagoflex.data.remote.RegistroCompromisoRequest
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.remote.aCompromiso
import cl.duocuc.pagoflex.data.remote.aEmpresa
import cl.duocuc.pagoflex.data.remote.aPagina
import cl.duocuc.pagoflex.data.remote.aResumenCobranza
import cl.duocuc.pagoflex.data.remote.aResumenMes
import cl.duocuc.pagoflex.data.remote.llamar
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Empresa
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.Pagina
import cl.duocuc.pagoflex.model.ResumenCobranza
import cl.duocuc.pagoflex.model.ResumenMes
import cl.duocuc.pagoflex.util.Fechas
import java.time.LocalDate
import java.time.YearMonth

class CompromisoRepository(
    private val api: CompromisoApi = RetrofitClient.crear(CompromisoApi::class.java)
) {

    suspend fun listarDeUsuario(usuarioId: Long, estado: String?, buscar: String, pagina: Int, tamano: Int = 5): Pagina<Compromiso> =
        llamar { api.listar(usuarioId, null, estado, buscar.trim().ifBlank { null }, null, pagina, tamano) }
            .aPagina { it.aCompromiso() }

    suspend fun listarDeEmpresa(empresaId: Long, estado: String?, mes: YearMonth?, buscar: String, pagina: Int, tamano: Int = 5): Pagina<Compromiso> =
        llamar { api.listar(null, empresaId, estado, buscar.trim().ifBlank { null }, mes?.toString(), pagina, tamano) }
            .aPagina { it.aCompromiso() }

    suspend fun obtener(id: Long): Compromiso =
        llamar { api.obtener(id) }.aCompromiso()

    suspend fun cuotas(id: Long): List<Compromiso> =
        llamar { api.cuotas(id) }.map { it.aCompromiso() }

    suspend fun motivoNoPagable(id: Long): String? {
        val respuesta = llamar { api.pagable(id) }
        return if (respuesta.pagable) null else respuesta.motivo
    }

    suspend fun resumenMes(usuarioId: Long): ResumenMes =
        llamar { api.resumenMes(usuarioId) }.aResumenMes()

    suspend fun proximos(usuarioId: Long, dias: Int): List<Compromiso> =
        llamar { api.proximos(usuarioId, dias) }.map { it.aCompromiso() }

    suspend fun resumenCobranza(empresaId: Long, mes: YearMonth): ResumenCobranza =
        llamar { api.resumenCobranza(empresaId, mes.toString()) }.aResumenCobranza()

    suspend fun registrar(
        empresaId: Long,
        ejecutivoId: Long,
        rut: String,
        nombre: String?,
        correo: String?,
        telefono: String?,
        concepto: String,
        numeroCuota: Int?,
        totalCuotas: Int?,
        monto: Int,
        vencimiento: LocalDate
    ): Compromiso {
        val datos = RegistroCompromisoRequest(
            empresaId = empresaId,
            ejecutivoId = ejecutivoId,
            rut = rut,
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            concepto = concepto.trim(),
            numeroCuota = numeroCuota,
            totalCuotas = totalCuotas,
            monto = monto,
            vencimiento = Fechas.aApi(vencimiento)
        )
        return llamar { api.registrar(datos) }.aCompromiso()
    }

    suspend fun anular(id: Long, motivo: MotivoAnulacion, comentario: String, ejecutivoId: Long): Compromiso =
        llamar { api.anular(id, AnularRequest(motivo.name, comentario.trim(), ejecutivoId)) }.aCompromiso()

    suspend fun eliminar(id: Long) =
        llamar { api.eliminar(id) }

    suspend fun empresa(id: Long): Empresa =
        llamar { api.empresa(id) }.aEmpresa()
}
