package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EcoColorScheme = lightColorScheme(
  primary = PlantnerGreenPrimary,
  onPrimary = PlantnerOnPrimary,
  primaryContainer = PlantnerGreenSecondary,
  onPrimaryContainer = PlantnerGreenPrimary,
  secondary = PlantnerGreenPrimary,
  onSecondary = PlantnerOnPrimary,
  secondaryContainer = PlantnerGreenSecondary,
  onSecondaryContainer = PlantnerGreenPrimary,
  background = PlantnerBg,
  onBackground = PlantnerOnBg,
  surface = PlantnerSurface,
  onSurface = PlantnerOnSurface,
  surfaceVariant = PlantnerBg,
  onSurfaceVariant = PlantnerMutedText,
  outline = PlantnerOutline,
  error = PlantnerAlertRed,
  errorContainer = PlantnerLightRedBg,
  onErrorContainer = PlantnerAlertRed
)

private val DarkEcoColorScheme = darkColorScheme(
  primary = Color(0xFF5CD9A2), // Brighter emerald green for high contrast dark theme
  onPrimary = Color(0xFF003920),
  primaryContainer = Color(0xFF005231),
  onPrimaryContainer = Color(0xFF88F6BE),
  secondary = Color(0xFF5CD9A2),
  onSecondary = Color(0xFF003920),
  secondaryContainer = Color(0xFF005231),
  onSecondaryContainer = Color(0xFF88F6BE),
  background = Color(0xFF111411), // Midnight obsidian background
  onBackground = Color(0xFFE2E3E2),
  surface = Color(0xFF191C19), // Elevated dark cards
  onSurface = Color(0xFFE2E3E2),
  surfaceVariant = Color(0xFF232B25), // Forest-obsidian variant
  onSurfaceVariant = Color(0xFFC0C4C0),
  outline = Color(0xFF404440),
  error = Color(0xFFFFB4AB),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val schemes = if (darkTheme) DarkEcoColorScheme else EcoColorScheme
  MaterialTheme(
    colorScheme = schemes,
    typography = Typography,
    content = content
  )
}
