package com.example.cambiateapp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    val colores = MaterialTheme.colorScheme
    Surface(
        selected = seleccionado,
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = if (seleccionado) colores.primary else colores.surface,
        contentColor = if (seleccionado) colores.onPrimary else colores.onSurface,
        border = if (seleccionado) null else BorderStroke(1.dp, colores.outline)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
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
            CategoryChip(texto = "Tops", seleccionado = false, onClick = {})
            CategoryChip(texto = "Pantalones", seleccionado = false, onClick = {})
        }
    }
}