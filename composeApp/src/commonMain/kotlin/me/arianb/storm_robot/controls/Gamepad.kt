package me.arianb.storm_robot.controls

// TODO: add platform-specific rumble method? for multi-platform rumble support?
//expect fun rumbleGamepad()

expect suspend fun pollGamepad()

// FIXME: implement controller input

// NOTE: one important thing that may cause cascading changes to the implementations is that this is callback-based, but
//       in some situations, we need to track the last provided state of a different callback. An example of this would
//       be if I wanted Left Joystick to act differently while Button A is pressed. This "last state" tracking can
//       be done at the platform-specific implementation level, or here. So far, I've been doing it at the lower level,
//       but it could be moved here. Just wanted to document the context for some of my design decisions here.
object Gamepad {
    fun onButtonA(isPressed: Boolean) {}
    fun onButtonB(isPressed: Boolean) {}
    fun onButtonX(isPressed: Boolean) {}
    fun onButtonY(isPressed: Boolean) {}

    fun onButtonLeftJoystick(isPressed: Boolean) {}
    fun onButtonRightJoystick(isPressed: Boolean) {}

    fun onDpad(direction: DPAD) {}

    fun leftJoystickCallback(x: Short, y: Short) {
        ControlSender.sendMovement(x, y)
    }

    fun rightJoystickCallback(x: Short, y: Short) {

    }
}

enum class DPAD {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}
