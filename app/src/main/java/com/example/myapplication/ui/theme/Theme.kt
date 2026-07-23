package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BlueGreen,         // #219ebc
    secondary = PrussianBlue,    // #023047
    tertiary = SelectiveYellow,  // #ffb703
    background = SkyBlue,        // #8ecae6
    surface = SkyBlue,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PrussianBlue,
    onBackground = PrussianBlue,
    onSurface = PrussianBlue,
    surfaceVariant = PureWhite,
    outline = PrussianBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    secondary = SelectiveYellow,
    tertiary = StrongOrange,
    background = PrussianBlue,
    surface = PrussianBlue,
    onPrimary = PrussianBlue,
    onSecondary = PrussianBlue,
    onTertiary = PrussianBlue,
    onBackground = PureWhite,
    onSurface = PureWhite,
    outline = BlueGreen
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.secondary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
