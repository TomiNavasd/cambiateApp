package com.example.cambiateapp.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class BotonPlaceholder(val texto: String, val accion: () -> Unit)

/** TEMPORAL: pantalla de reemplazo hasta que se enchufen las reales. */
@Composable
fun PantallaPlaceholder(
    titulo: String,
    botones: List<BotonPlaceholder>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineSmall)
        botones.forEach { boton ->
            Button(onClick = boton.accion) { Text(boton.texto) }
        }
    }
}