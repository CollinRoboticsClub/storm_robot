package me.arianb.storm_robot.controls

import android.view.InputDevice
import android.view.MotionEvent
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import kotlin.math.abs

actual suspend fun pollGamepad() {
    // FIXME: implement
}

fun onGenericMotionEventHelper(event: MotionEvent): Boolean {
    // Check that the event came from a game controller
    if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
        && event.action == MotionEvent.ACTION_MOVE
    ) {

        // Process the movements starting from the
        // earliest historical position in the batch
        (0 until event.historySize).forEach { i ->
            // Process the event at historical position i
            processControllerMotionEvent(event, i)
        }

        // Process the current movement sample in the batch (position -1)
        processControllerMotionEvent(event, -1)
        return true
    }

    return false
}

private fun processControllerMotionEvent(event: MotionEvent, historyPos: Int) {
    val inputDevice = event.device

    val leftJoystickX: Short = mapFloatRangeToShort(
        getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X, historyPos)
    )
    val leftJoystickY: Short = mapFloatRangeToShort(
        getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y, historyPos)
    )

    val rightJoystickX: Short = mapFloatRangeToShort(
        getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Z, historyPos)
    )
    val rightJoystickY: Short = mapFloatRangeToShort(
        getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ, historyPos)
    )

    Gamepad.leftJoystickCallback(leftJoystickX, leftJoystickY)
    Gamepad.rightJoystickCallback(rightJoystickX, rightJoystickY)

    Logger.ANDROID.log("L: ($leftJoystickX, $leftJoystickY) \t | \t R: ($rightJoystickX, $rightJoystickY)")
}

private fun getCenteredAxis(
    event: MotionEvent,
    device: InputDevice,
    axis: Int,
    historyPos: Int
): Float {
    val range: InputDevice.MotionRange? = device.getMotionRange(axis, event.source)

    // A joystick at rest does not always report an absolute position of
    // (0,0). Use the getFlat() method to determine the range of values
    // bounding the joystick axis center.
    range?.apply {
        val value: Float = if (historyPos < 0) {
            event.getAxisValue(axis)
        } else {
            event.getHistoricalAxisValue(axis, historyPos)
        }

        // Ignore axis values that are within the 'flat' region of the
        // joystick axis center.
        if (abs(value) > flat) {
            return value
        }
    }

    return 0f
}

// Map float in range [-1.0, 1.0] to a Short
fun mapFloatRangeToShort(float: Float): Short =
    (float * Short.MAX_VALUE).toInt().toShort()
