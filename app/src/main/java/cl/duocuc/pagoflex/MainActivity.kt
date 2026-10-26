package cl.duocuc.pagoflex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duocuc.pagoflex.navigation.AppNavegacion
import cl.duocuc.pagoflex.ui.theme.PagoFlexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PagoFlexTheme {
                AppNavegacion()
            }
        }
    }
}
