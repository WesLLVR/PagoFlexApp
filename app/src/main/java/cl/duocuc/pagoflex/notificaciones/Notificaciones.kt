package cl.duocuc.pagoflex.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duocuc.pagoflex.MainActivity
import cl.duocuc.pagoflex.R

object Notificaciones {

    const val CANAL_VENCIMIENTOS = "vencimientos"
    const val CANAL_CUENTA = "cuenta"
    const val ID_CODIGO = 9000

    fun crearCanales(context: Context) {
        val gestor = context.getSystemService(NotificationManager::class.java)
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_VENCIMIENTOS,
                context.getString(R.string.canal_vencimientos),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.canal_vencimientos_descripcion) }
        )
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_CUENTA,
                context.getString(R.string.canal_cuenta),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.canal_cuenta_descripcion) }
        )
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun mostrar(context: Context, id: Int, canal: String, titulo: String, texto: String): Boolean {
        val gestor = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        if (!gestor.areNotificationsEnabled()) return false
        crearCanales(context)
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_aviso)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        gestor.notify(id, notificacion)
        return true
    }
}
