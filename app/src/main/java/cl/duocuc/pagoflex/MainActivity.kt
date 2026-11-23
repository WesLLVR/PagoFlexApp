package cl.duocuc.pagoflex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cl.duocuc.pagoflex.data.ServidorPreferencias
import cl.duocuc.pagoflex.data.remote.RetrofitClient
import cl.duocuc.pagoflex.navigation.AppNavegacion
import cl.duocuc.pagoflex.notificaciones.Notificaciones
import cl.duocuc.pagoflex.ui.theme.PagoFlexTheme
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.cambiarServidor(ServidorPreferencias(this).leer())
        SingletonImageLoader.setSafe { contexto ->
            ImageLoader.Builder(contexto)
                .components { add(OkHttpNetworkFetcherFactory(callFactory = { RetrofitClient.cliente })) }
                .build()
        }
        Notificaciones.crearCanales(this)
        setContent {
            PagoFlexTheme {
                AppNavegacion()
            }
        }
    }
}
