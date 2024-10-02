package me.arianb.storm_robot.controls

expect suspend fun pollGamepad()

// FIXME: implement controller input
object Gamepad {
    fun onButtonA() {}
    fun onButtonB() {}
    fun onButtonX() {}
    fun onButtonY() {}

    fun onButtonLeftJoystick() {}
    fun onButtonRightJoystick() {}

    fun onDpad() {}

    fun leftJoystickCallback(value: Short) {

    }

    fun rightJoystickCallback(value: Short) {

    }
}
