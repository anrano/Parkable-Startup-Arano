package com.parkable.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = ParkableBluePrimary,
    onPrimary = SurfaceLight,
    primaryContainer = ParkableBlueLight,
    onPrimaryContainer = ParkableBlueDark,
    secondary = ParkableGreen,
    onSecondary = SurfaceLight,
    secondaryContainer = ParkableGreenLight,
    onSecondaryContainer = ParkableGreenDark,
    tertiary = ParkableTeal,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE5EEF6),
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = ParkableBlueLight,
    onPrimary = BackgroundDark,
    primaryContainer = ParkableBlueDark,
    onPrimaryContainer = ParkableBlueLight,
    secondary = ParkableGreenLight,
    onSecondary = BackgroundDark,
    secondaryContainer = ParkableGreenDark,
    onSecondaryContainer = ParkableGreenLight,
    tertiary = ParkableTeal,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF1A3349),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorRed,
    onError = SurfaceDark
)

/** Tema raíz de la app. Acepta override explícito para soportar la opción "system/light/dark". */
@Composable
fun ParkableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = ParkableTypography,
        content = content
    )
}

/** Gradientes reutilizables que dan a la app el aire "futurista + ecológico" pedido. */
object ParkableGradients {
    val brandHorizontal: Brush
        @Composable get() = Brush.horizontalGradient(
            listOf(ParkableBluePrimary, ParkableTeal, ParkableGreen)
        )

    val brandVertical: Brush
        @Composable get() = Brush.verticalGradient(
            listOf(ParkableBluePrimary, ParkableGreen)
        )

    val backgroundSubtle: Brush
        @Composable get() = Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        )
}
