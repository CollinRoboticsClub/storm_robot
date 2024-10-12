package me.arianb.storm_robot.controls

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger

// FIXME: implement
fun onKeyCallback(event: KeyEvent): Boolean {
    if (event.key == Key.Unknown) {
        print("unknown: ")
    }
    Logger.DEFAULT.log(event.toString())
    return false
}
