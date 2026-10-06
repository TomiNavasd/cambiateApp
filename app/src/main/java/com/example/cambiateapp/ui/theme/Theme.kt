package com.example.cambiateapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = BackgroundWhite,
    primaryContainer = Tint,
    onPrimaryContainer = Primary,
    secondary = Secondary,
    onSecondary = TextPrimary,
    secondaryContainer = Tint,
    onSecondaryContainer = Primary,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = BackgroundWhite,
    surfaceContainerLow = SurfaceLight,
    surfaceContainer = SurfaceLight,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHigh,
    outline = OutlineGray,
    outlineVariant = OutlineVariantGray,
    error = ErrorRed,
    onError = BackgroundWhite
)

// Solo tema claro y sin colores dinámicos: así la app se ve siempre con nuestra paleta.
@Composable
fun CambiateAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}