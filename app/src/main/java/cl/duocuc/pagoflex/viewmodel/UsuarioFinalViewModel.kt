package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.AvisosPreferencias
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.PagoRepository
import cl.duocuc.pagoflex.data.repository.ReporteRepository
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.ReglasNegocio
import cl.duocuc.pagoflex.model.Aviso
import cl.duocuc.pagoflex.model.Compromiso
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.EstadoCompromiso
import cl.duocuc.pagoflex.model.Pago
import cl.duocuc.pagoflex.model.Reporte
import cl.duocuc.pagoflex.model.ResumenMes
import cl.duocuc.pagoflex.model.Usuario
import cl.duocuc.pagoflex.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsuarioFinalUiState(
    val usuario: Usuario? = null,
    val compromisos: List<Compromiso> = emptyList(),
    val resumen: ResumenMes = ResumenMes(),
    val avisos: List<Aviso> = emptyList(),
    val cumplimiento: Cumplimiento = Cumplimiento(),
    val ultimaActualizacion: String? = null
) {
    val vigentes: List<Compromiso>
        get() = compromisos.filter {
            it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO ||
                it.estado == EstadoCompromiso.EN_REVISION
        }
}

class UsuarioFinalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val usuarios = UsuarioRepository(db)
    private val compromisos = CompromisoRepository(db)
    private val pagos = PagoRepository(db)
    private val reportes = ReporteRepository(db)
    private val sesion = SesionPreferencias(application)
    private val avisosPreferencias = AvisosPreferencias(application)

    private val _uiState = MutableStateFlow(UsuarioFinalUiState())
    val uiState: StateFlow<UsuarioFinalUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        val usuario = usuarios.buscarPorId(sesion.leer())
        if (usuario == null) {
            _uiState.value = UsuarioFinalUiState()
            return
        }
        val lista = compromisos.listarDeUsuario(usuario.id)
        sesion.guardarUltimaActualizacion(Fechas.mostrar(Fechas.ahora()))
        _uiState.value = UsuarioFinalUiState(
            usuario = usuario,
            compromisos = lista,
            resumen = ReglasNegocio.resumenMes(lista),
            avisos = ReglasNegocio.avisos(lista, avisosPreferencias.leer()),
            cumplimiento = compromisos.cumplimiento(usuario.id),
            ultimaActualizacion = sesion.leerUltimaActualizacion()
        )
    }

    fun compromiso(id: Long): Compromiso? = _uiState.value.compromisos.firstOrNull { it.id == id }

    fun motivoNoPagable(compromiso: Compromiso): String? =
        ReglasNegocio.motivoNoPagable(compromiso, _uiState.value.compromisos)

    fun reporteDe(idCompromiso: Long): Reporte? = reportes.ultimoDeCompromiso(idCompromiso)

    fun pagoDe(idCompromiso: Long): Pago? = pagos.buscarPorCompromiso(idCompromiso)

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
