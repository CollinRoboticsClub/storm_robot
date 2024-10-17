package me.arianb.storm_robot

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

                val controlSenderScope = CoroutineScope(Dispatchers.IO)
                val controlSenderJob = remember {
                    RestartableJob(
                        coroutineScope = controlSenderScope,
                        block = {
                            ControlSender.start(onConnectionError = {}, onErrorInBlock = {})

                            Logger.DEFAULT.log("oh no, ControlSender.start() has exited.")
                        },
                        handler = CoroutineExceptionHandler { _, exception ->
                            Logger.DEFAULT.log(
                                exception.message ?: "null"
                            )
                        }
                    )
                }
            }
        )
    }
}
