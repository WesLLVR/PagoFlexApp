package cl.duocuc.pagoflex.notificaciones

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cl.duocuc.pagoflex.R
import cl.duocuc.pagoflex.data.AvisosPreferencias
import cl.duocuc.pagoflex.data.ServidorPreferencias
import cl.duocuc.pagoflex.data.SesionPreferencias
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.data.repository.CompromisoRepository
import cl.duocuc.pagoflex.logica.ReglasNegocio
import cl.duocuc.pagoflex.model.Rol
import java.util.concurrent.TimeUnit

class AvisosWorker(
    context: Context,
    parametros: WorkerParameters
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        RetrofitClient.cambiarServidor(ServidorPreferencias(applicationContext).leer())
        val sesion = SesionPreferencias(applicationContext)
        if (!sesion.haySesion() || sesion.rol() != Rol.USUARIO_FINAL) return Result.success()
        val configuracion = AvisosPreferencias(applicationContext).leer()
        if (!configuracion.activos) return Result.success()
        return try {
            val proximos = CompromisoRepository().proximos(sesion.idUsuario(), configuracion.diasAnticipacion)
            ReglasNegocio.avisos(proximos, configuracion).forEach { aviso ->
                Notificaciones.mostrar(
                    context = applicationContext,
                    id = aviso.idCompromiso.toInt(),
                    canal = Notificaciones.CANAL_VENCIMIENTOS,
                    titulo = applicationContext.getString(R.string.notificacion_vencimiento_titulo),
                    texto = aviso.mensaje
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val TRABAJO_DIARIO = "avisos_diarios"
        private const val TRABAJO_AHORA = "avisos_ahora"

        private val conInternet = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun programar(context: Context) {
            val diario = PeriodicWorkRequestBuilder<AvisosWorker>(1, TimeUnit.DAYS)
                .setConstraints(conInternet)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TRABAJO_DIARIO, ExistingPeriodicWorkPolicy.KEEP, diario)
        }

        fun revisarAhora(context: Context) {
            val ahora = OneTimeWorkRequestBuilder<AvisosWorker>()
                .setConstraints(conInternet)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(TRABAJO_AHORA, ExistingWorkPolicy.REPLACE, ahora)
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_DIARIO)
        }
    }
}
