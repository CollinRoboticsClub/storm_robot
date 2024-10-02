package me.arianb.storm_robot

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import me.arianb.storm_robot.controls.onKeyCallback
import me.arianb.storm_robot.controls.pollGamepad

fun main() = application {
    Window(
        title = "STORM Robot Client",
        onCloseRequest = ::exitApplication,
        onKeyEvent = ::onKeyCallback
    ) {
        val topLevelCoroutineScope = rememberCoroutineScope()
        topLevelCoroutineScope.launch {
            pollGamepad()
        }
        App()
    }
}
