package com.example.cambiateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cambiateapp.presentation.navigation.AppRoot
import com.example.cambiateapp.presentation.navigation.SessionViewModel
import com.example.cambiateapp.ui.theme.CambiateAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CambiateAppTheme {
                val estado by sessionViewModel.uiState.collectAsStateWithLifecycle()
                AppRoot(
                    estado = estado,
                    onEvent = sessionViewModel::onEvent
                )
            }
        }
    }
}