package me.arianb.storm_robot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    // TODO: Implement if I want theme to be switchable via menu
//    val preferencesState by settingsViewModel.userPreferencesFlow.collectAsState()
//    val darkTheme = when (preferencesState.appTheme) {
//        AppTheme.DarkMode -> true
//        AppTheme.LightMode -> false
//        else -> isSystemInDarkTheme()
//    }
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.background.toArgb()
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
