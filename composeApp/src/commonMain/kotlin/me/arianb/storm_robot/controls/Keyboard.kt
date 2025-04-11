package me.arianb.storm_robot.controls

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import co.touchlab.kermit.Logger

// FIXME: implement
fun onKeyCallback(event: KeyEvent): Boolean {
    if (event.key == Key.Unknown) {
        print("unknown: ")
    }
    Logger.d(event.toString())
    return false
}
