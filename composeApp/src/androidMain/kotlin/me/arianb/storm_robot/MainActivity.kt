package me.arianb.storm_robot

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.arianb.storm_robot.controls.onGenericMotionEventHelper
import me.arianb.storm_robot.controls.onKeyCallback
import androidx.compose.ui.input.key.KeyEvent as ComposeKeyEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return onKeyCallback(ComposeKeyEvent(event)) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return onKeyCallback(ComposeKeyEvent(event)) || super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return onGenericMotionEventHelper(event) || super.onGenericMotionEvent(event)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
