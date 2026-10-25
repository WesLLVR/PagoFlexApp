package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Empresa
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.MotivoAnulacion
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.ResumenCobranza
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.YearMonth

data class EjecutivoUiState(
    val empresa: Empresa? = null,
    val compromisos: List<Compromiso> = emptyList(),
    val filtro: EstadoCompromiso? = null,
    val filtroMes: YearMonth? = null,
    val mes: YearMonth = YearMonth.from(Fechas.hoy()),
    val resumen: ResumenCobranza = ResumenCobranza(),
    val motivoAnulacion: MotivoAnulacion? = null,
    val comentarioAnulacion: String = "",
    val errorMotivo: String? = null,
    val errorComentario: String? = null
) {
    val compromisosFiltrados: List<Compromiso>
        get() = compromisos.filter {
            (filtro == null || it.estado == filtro) &&
                (filtroMes == null || YearMonth.from(it.vencimiento) == filtroMes)
        }

    val mesesDisponibles: List<YearMonth>
        get() = compromisos.map { YearMonth.from(it.vencimiento) }.distinct().sortedDescending()
}

class EjecutivoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val usuarios = UsuarioRepository(db)
    private val compromisos = CompromisoRepository(db)
    private val reportes = ReporteRepository(db)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(EjecutivoUiState())
    val uiState: StateFlow<EjecutivoUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        val idEmpresa = usuarios.buscarPorId(sesion.leer())?.idEmpresa ?: return
        val mes = _uiState.value.mes
        _uiState.update {
            it.copy(
                empresa = compromisos.buscarEmpresa(idEmpresa),
                compromisos = compromisos.listarDeEmpresa(idEmpresa),
                resumen = compromisos.resumenCobranza(idEmpresa, mes)
            )
        }
    }

    fun onFiltroSeleccionado(estado: EstadoCompromiso?) {
        _uiState.update { it.copy(filtro = estado) }
    }

    fun onFiltroMesSeleccionado(mes: YearMonth?) {
        _uiState.update { it.copy(filtroMes = mes) }
    }

    fun onMesAnterior() {
        _uiState.update { it.copy(mes = it.mes.minusMonths(1)) }
        recargar()
    }

    fun onMesSiguiente() {
        _uiState.update { it.copy(mes = it.mes.plusMonths(1)) }
        recargar()
    }

    fun compromiso(id: Long): Compromiso? = _uiState.value.compromisos.firstOrNull { it.id == id }

    fun reporteDe(idCompromiso: Long): Reporte? = reportes.ultimoDeCompromiso(idCompromiso)

    fun prepararAnulacion() {
        _uiState.update {
            it.copy(motivoAnulacion = null, comentarioAnulacion = "", errorMotivo = null, errorComentario = null)
        }
    }

    fun onMotivoAnulacion(motivo: MotivoAnulacion) {
        _uiState.update { it.copy(motivoAnulacion = motivo, errorMotivo = null, errorComentario = null) }
    }

    fun onComentarioAnulacion(valor: String) {
        _uiState.update { it.copy(comentarioAnulacion = valor, errorComentario = null) }
    }

    fun anular(compromiso: Compromiso): Boolean {
        val estado = _uiState.value
        val errorMotivo = Validador.motivoAnulacion(estado.motivoAnulacion)
        val errorComentario = Validador.comentarioAnulacion(estado.motivoAnulacion, estado.comentarioAnulacion)
        _uiState.update { it.copy(errorMotivo = errorMotivo, errorComentario = errorComentario) }
        val motivo = estado.motivoAnulacion
        if (errorMotivo != null || errorComentario != null || motivo == null || !compromiso.sePuedeAnular) return false
        compromisos.anular(compromiso.id, motivo, estado.comentarioAnulacion, sesion.leer())
        recargar()
        return true
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
