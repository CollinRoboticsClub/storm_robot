package me.arianb.storm_robot

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import me.arianb.storm_robot.dashboard.DashboardScreen
import me.arianb.storm_robot.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        Scaffold(
            content = { contentPadding ->
                DashboardScreen(contentPadding)
            }
        )
    }
}
