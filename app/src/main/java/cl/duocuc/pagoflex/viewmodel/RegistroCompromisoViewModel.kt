package cl.duocuc.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.pagoflex.data.PagoFlexDbHelper
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.data.repository.UsuarioRepository
import cl.duocuc.pagoflex.logica.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistroCompromisoUiState(
    val rut: String = "",
    val nombrePersona: String? = null,
    val personaNueva: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val concepto: String = "",
    val monto: String = "",
    val vencimiento: String = "",
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
    val registrado: Boolean = false
)

class RegistroCompromisoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PagoFlexDbHelper(application)
    private val usuarios = UsuarioRepository(db)
    private val compromisos = CompromisoRepository(db)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(RegistroCompromisoUiState())
    val uiState: StateFlow<RegistroCompromisoUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) {
        val rutValido = Validador.rut(valor) == null
        val persona = if (rutValido) usuarios.buscarUsuarioFinalPorRut(Validador.formatearRut(valor)) else null
        _uiState.update {
            it.copy(
                rut = valor,
                nombrePersona = persona?.nombre,
                personaNueva = rutValido && persona == null,
                errorRut = null,
                errorGeneral = null,
                registrado = false
            )
        }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registrado = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registrado = false) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor.trim(), errorTelefono = null, registrado = false) }
    }

    fun onConceptoCambia(valor: String) {
        _uiState.update { it.copy(concepto = valor, errorConcepto = null, errorGeneral = null, registrado = false) }
    }

    fun onMontoCambia(valor: String) {
        _uiState.update { it.copy(monto = valor.trim(), errorMonto = null, registrado = false) }
    }

    fun onVencimientoCambia(valor: String) {
        _uiState.update { it.copy(vencimiento = valor.trim(), errorVencimiento = null, registrado = false) }
    }

    fun onCuotaCambia(valor: String) {
        _uiState.update { it.copy(cuota = valor.trim(), errorCuota = null, errorGeneral = null, registrado = false) }
    }

    fun onTotalCuotasCambia(valor: String) {
        _uiState.update { it.copy(totalCuotas = valor.trim(), errorTotalCuotas = null, registrado = false) }
    }

    fun onRegistrar(): Boolean {
        val estado = _uiState.value
        val ejecutivo = usuarios.buscarPorId(sesion.leer()) ?: return false
        val idEmpresa = ejecutivo.idEmpresa ?: return false

        val errorRut = Validador.rut(estado.rut)
        val persona = if (errorRut == null) usuarios.buscarUsuarioFinalPorRut(Validador.formatearRut(estado.rut)) else null
        val personaNueva = errorRut == null && persona == null

        val errorNombre = if (personaNueva) Validador.nombre(estado.nombre) else null
        val errorCorreo = if (personaNueva) Validador.correo(estado.correo) else null
        val errorTelefono = if (personaNueva) Validador.telefono(estado.telefono) else null
        val errorConcepto = Validador.concepto(estado.concepto)
        val errorMonto = Validador.monto(estado.monto)
        val errorVencimiento = Validador.fechaVencimiento(estado.vencimiento)
        val (errorCuota, errorTotal) = Validador.cuotas(estado.cuota, estado.totalCuotas)

        val hayErrores = listOf(
            errorRut, errorNombre, errorCorreo, errorTelefono,
            errorConcepto, errorMonto, errorVencimiento, errorCuota, errorTotal
        ).any { it != null }

        val duplicado = !hayErrores && persona != null &&
            compromisos.existeDuplicado(persona.id, idEmpresa, estado.concepto, estado.cuota.toIntOrNull())

        _uiState.update {
            it.copy(
                personaNueva = personaNueva,
                errorRut = errorRut,
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorTelefono = errorTelefono,
                errorConcepto = errorConcepto,
                errorMonto = errorMonto,
                errorVencimiento = errorVencimiento,
                errorCuota = errorCuota,
                errorTotalCuotas = errorTotal,
                errorGeneral = if (duplicado) "Ya existe este compromiso para la persona (misma empresa, concepto y cuota)" else null
            )
        }
        if (hayErrores || duplicado) return false

        val vencimiento = Validador.fechaDesdeTexto(estado.vencimiento) ?: return false
        val idPersona = persona?.id ?: usuarios.registrarUsuarioFinal(
            nombre = estado.nombre,
            rut = Validador.formatearRut(estado.rut),
            correo = estado.correo,
            telefono = estado.telefono
        )
        compromisos.registrar(
            idUsuario = idPersona,
            idEmpresa = idEmpresa,
            concepto = estado.concepto,
            numeroCuota = estado.cuota.toIntOrNull(),
            totalCuotas = estado.totalCuotas.toIntOrNull(),
            monto = estado.monto.toInt(),
            vencimiento = vencimiento,
            idEjecutivo = ejecutivo.id
        )
        _uiState.value = RegistroCompromisoUiState(registrado = true)
        return true
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
