package me.arianb.storm_robot

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.arianb.storm_robot.controls.ControlSender
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

                val coroutineScope = rememberCoroutineScope()
                coroutineScope.launch {
                    while (isActive) {
                        ControlSender.start()

                        Logger.DEFAULT.log("oh no, ControlSender.start() has exited. Attempting to restart...")
                    }
                }
            }
        )
    }
}
