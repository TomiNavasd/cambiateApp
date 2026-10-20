package com.example.cambiateapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnPrimaryDark,
    secondaryContainer = PrimaryContainerDark,
    onSecondaryContainer = OnPrimaryContainerDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceHighDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = BackgroundDark,
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceHighDark,
    surfaceContainerHighest = SurfaceHighDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark
)

// Sin colores dinámicos: así la app se ve siempre con nuestra paleta, en claro o en oscuro.
// Quien llama decide si es oscuro (según lo que eligió el usuario); por defecto sigue al sistema.
@Composable
fun CambiateAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}