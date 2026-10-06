package com.example.cambiateapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cambiateapp.ui.theme.CambiateAppTheme

@Composable
fun CategoryChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(text = texto) },
        shape = CircleShape,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun CategoryChipPreview() {
    CambiateAppTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CategoryChip(texto = "Todas", seleccionado = true, onClick = {})
            CategoryChip(texto = "Superior", seleccionado = false, onClick = {})
        }
    }
}