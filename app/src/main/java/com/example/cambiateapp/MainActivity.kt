package com.example.cambiateapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.domain.model.ModoTema
import com.example.cambiateapp.presentation.navigation.AppRoot
import com.example.cambiateapp.presentation.navigation.SessionViewModel
import com.example.cambiateapp.presentation.theme.TemaViewModel
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import dagger.hilt.android.AndroidEntryPoint

/** Fondos semitransparentes de la barra de navegación, según el tema. */
private val FondoBarraClaro = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val FondoBarraOscuro = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()
    private val temaViewModel: TemaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val modo by temaViewModel.modo.collectAsStateWithLifecycle()
            val oscuro = when (modo) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            // Los íconos de la barra de estado y de navegación tienen que verse bien
            // con el tema elegido, aunque no coincida con el del sistema.
            DisposableEffect(oscuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(FondoBarraClaro, FondoBarraOscuro) { oscuro },
                )
                onDispose { }
            }

            CambiateAppTheme(darkTheme = oscuro) {
                val estado by sessionViewModel.uiState.collectAsStateWithLifecycle()
                AppRoot(
                    estado = estado,
                    onEvent = sessionViewModel::onEvent
                )
            }
        }
    }
}