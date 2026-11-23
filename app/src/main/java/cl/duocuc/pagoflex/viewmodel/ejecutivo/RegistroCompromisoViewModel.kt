package cl.duocuc.pagoflex.viewmodel.ejecutivo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.viewmodel.ejecutar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class RegistroCompromisoUiState(
    val rut: String = "",
    val nombrePersona: String? = null,
    val personaNueva: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val concepto: String = "",
    val monto: String = "",
    val vencimiento: LocalDate? = null,
    val cuota: String = "",
    val totalCuotas: String = "",
    val errorRut: String? = null,
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorConcepto: String? = null,
    val errorMonto: String? = null,
    val errorVencimiento: String? = null,
    val errorCuota: String? = null,
    val errorTotalCuotas: String? = null,
    val errorGeneral: String? = null,
    val procesando: Boolean = false,
    val registrado: String? = null
)

class RegistroCompromisoViewModel(application: Application) : AndroidViewModel(application) {

    private val compromisos = CompromisoRepository()
    private val usuarios = UsuarioRepository()
    private val sesion = SesionPreferencias(application)
    private var busqueda: Job? = null

    private val _uiState = MutableStateFlow(RegistroCompromisoUiState())
    val uiState: StateFlow<RegistroCompromisoUiState> = _uiState.asStateFlow()

    fun onRutCambia(texto: String) {
        _uiState.update {
            it.copy(rut = texto, errorRut = null, nombrePersona = null, personaNueva = false, errorGeneral = null)
        }
        busqueda?.cancel()
        if (Validador.rut(texto) != null) return
        val rut = Validador.formatearRut(texto)
        busqueda = ejecutar(onError = { mensaje -> _uiState.update { it.copy(errorGeneral = mensaje) } }) {
            val persona = usuarios.buscarPorRut(rut)
            _uiState.update { it.copy(nombrePersona = persona?.nombre, personaNueva = persona == null) }
        }
    }

    fun onNombreCambia(texto: String) = _uiState.update { it.copy(nombre = texto, errorNombre = null) }
    fun onCorreoCambia(texto: String) = _uiState.update { it.copy(correo = texto, errorCorreo = null) }
    fun onTelefonoCambia(texto: String) = _uiState.update { it.copy(telefono = texto, errorTelefono = null) }
    fun onConceptoCambia(texto: String) = _uiState.update { it.copy(concepto = texto, errorConcepto = null) }
    fun onMontoCambia(texto: String) = _uiState.update { it.copy(monto = texto, errorMonto = null) }
    fun onVencimientoElegido(fecha: LocalDate?) = _uiState.update { it.copy(vencimiento = fecha, errorVencimiento = null) }
    fun onCuotaCambia(texto: String) = _uiState.update { it.copy(cuota = texto, errorCuota = null) }
    fun onTotalCuotasCambia(texto: String) = _uiState.update { it.copy(totalCuotas = texto, errorTotalCuotas = null) }

    fun onRegistrar() {
        val estado = _uiState.value
        val nueva = estado.personaNueva
        val (errorCuota, errorTotal) = Validador.cuotas(estado.cuota, estado.totalCuotas)
        val errores = estado.copy(
            errorRut = Validador.rut(estado.rut),
            errorNombre = if (nueva) Validador.nombre(estado.nombre) else null,
            errorCorreo = if (nueva) Validador.correo(estado.correo) else null,
            errorTelefono = if (nueva) Validador.telefono(estado.telefono) else null,
            errorConcepto = Validador.concepto(estado.concepto),
            errorMonto = Validador.monto(estado.monto),
            errorVencimiento = Validador.fechaVencimiento(estado.vencimiento),
            errorCuota = errorCuota,
            errorTotalCuotas = errorTotal,
            registrado = null
        )
        _uiState.value = errores
        val hayErrores = listOf(
            errores.errorRut, errores.errorNombre, errores.errorCorreo, errores.errorTelefono, errores.errorConcepto,
            errores.errorMonto, errores.errorVencimiento, errores.errorCuota, errores.errorTotalCuotas
        ).any { it != null }
        val vencimiento = estado.vencimiento
        if (hayErrores || vencimiento == null) return

        _uiState.update { it.copy(procesando = true, errorGeneral = null) }
        ejecutar(onError = { mensaje -> _uiState.update { it.copy(procesando = false, errorGeneral = mensaje) } }) {
            val registrado = compromisos.registrar(
                empresaId = sesion.empresaId(),
                ejecutivoId = sesion.idUsuario(),
                rut = Validador.formatearRut(estado.rut),
                nombre = if (nueva) estado.nombre.trim() else null,
                correo = if (nueva) estado.correo.trim() else null,
                telefono = if (nueva) estado.telefono.trim() else null,
                concepto = estado.concepto,
                numeroCuota = estado.cuota.toIntOrNull(),
                totalCuotas = estado.totalCuotas.toIntOrNull(),
                monto = estado.monto.toInt(),
                vencimiento = vencimiento
            )
            val mensaje = if (nueva) {
                "Compromiso ${registrado.folio} registrado. ${registrado.nombreUsuario} podrá entrar a la app con \"Recuperar contraseña\" usando su correo."
            } else {
                "Compromiso ${registrado.folio} registrado. ${registrado.nombreUsuario} ya lo ve en su app."
            }
            _uiState.value = RegistroCompromisoUiState(registrado = mensaje)
        }
    }
}
