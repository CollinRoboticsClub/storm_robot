package me.arianb.storm_robot.controls

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

// FIXME: implement
fun onKeyCallback(event: KeyEvent): Boolean {
    if (event.key == Key.Unknown) {
        print("unknown: ")
    }
    println(event)
    return false
}
