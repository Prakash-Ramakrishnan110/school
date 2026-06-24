package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = HighDensityPrimaryDark,
    secondary = HighDensitySecondaryDark,
    tertiary = HighDensityPrimaryDark,
    background = HighDensityBackgroundDark,
    surface = HighDensitySurfaceDark,
    onPrimary = Color(0xFF003450),
    onBackground = Color(0xFFEFF1F3),
    onSurface = Color(0xFFEFF1F3),
    primaryContainer = HighDensityPrimaryContainerDark,
    onPrimaryContainer = HighDensityOnPrimaryContainerDark,
    secondaryContainer = HighDensitySecondaryContainerDark,
    onSecondaryContainer = Color(0xFFEFF1F3),
    outline = HighDensityOutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HighDensityPrimaryLight,
    secondary = HighDensitySecondaryLight,
    tertiary = HighDensityPrimaryLight,
    background = HighDensityBackgroundLight,
    surface = HighDensitySurfaceLight,
    onPrimary = Color.White,
    onBackground = Color(0xFF191C1E),
    onSurface = Color(0xFF191C1E),
    primaryContainer = HighDensityPrimaryContainerLight,
    onPrimaryContainer = HighDensityOnPrimaryContainerLight,
    secondaryContainer = HighDensitySecondaryContainerLight,
    onSecondaryContainer = Color(0xFF191C1E),
    outline = HighDensityOutlineLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic colors to enforce the specific High Density design palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
