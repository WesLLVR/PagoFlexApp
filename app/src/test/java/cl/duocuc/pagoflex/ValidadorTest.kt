package cl.duocuc.pagoflex

import cl.duocuc.pagoflex.logica.Validador
import cl.duocuc.pagoflex.model.Cumplimiento
import cl.duocuc.pagoflex.model.CategoriaCumplimiento
import cl.duocuc.pagoflex.model.MotivoReporte
import cl.duocuc.pagoflex.util.Fechas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorTest {

    @Test
    fun rutConDigitoCorrectoEsValido() {
        assertNull(Validador.rut("15.834.207-3"))
        assertNull(Validador.rut("16248317-k"))
    }

    @Test
    fun rutConDigitoIncorrectoEsRechazado() {
        assertNotNull(Validador.rut("15.834.207-4"))
        assertNotNull(Validador.rut(""))
    }

    @Test
    fun montoDebeSerEnteroYPositivo() {
        assertNull(Validador.monto("310000"))
        assertNotNull(Validador.monto("0"))
        assertNotNull(Validador.monto("31.000"))
    }

    @Test
    fun fechaNoPuedeSerAnteriorAHoy() {
        assertNotNull(Validador.fechaVencimiento("01-01-2020"))
        assertNotNull(Validador.fechaVencimiento("30-02-2030"))
        assertNull(Validador.fechaVencimiento(Fechas.mostrar(Fechas.hoy().plusDays(5))))
    }

    @Test
    fun cuotaNoPuedeSuperarElTotal() {
        assertNotNull(Validador.cuotas("37", "36").first)
        assertNull(Validador.cuotas("16", "36").first)
    }

    @Test
    fun motivoOtroExigeComentario() {
        assertNotNull(Validador.comentarioReporte(MotivoReporte.OTRO, ""))
        assertNull(Validador.comentarioReporte(MotivoReporte.YA_PAGUE, ""))
    }

    @Test
    fun cumplimientoSeCalculaComoEnElCaso() {
        val cumplimiento = Cumplimiento(considerados = 36, aTiempo = 33)
        assertEquals(91.7, cumplimiento.porcentaje!!, 0.05)
        assertEquals(CategoriaCumplimiento.AL_DIA, cumplimiento.categoria)
        assertEquals(CategoriaCumplimiento.SIN_HISTORIAL, Cumplimiento(2, 2).categoria)
    }
}
