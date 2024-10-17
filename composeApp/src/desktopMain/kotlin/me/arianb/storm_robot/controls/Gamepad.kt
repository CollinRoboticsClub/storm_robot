package me.arianb.storm_robot.controls

import io.github.libsdl4j.api.Sdl
import io.github.libsdl4j.api.Sdl.SDL_Quit
import io.github.libsdl4j.api.SdlSubSystemConst
import io.github.libsdl4j.api.error.SdlError
import io.github.libsdl4j.api.event.SDL_Event
import io.github.libsdl4j.api.event.SDL_EventType
import io.github.libsdl4j.api.event.SdlEvents.SDL_WaitEvent
import io.github.libsdl4j.api.event.SdlEventsConst
import io.github.libsdl4j.api.gamecontroller.SDL_GameController
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton
import io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*
import io.github.libsdl4j.api.joystick.SdlJoystick.SDL_NumJoysticks
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val GAMEPAD_POLLING_RATE_PER_SECOND: Long = 20
const val GAMEPAD_POLLING_DELAY_MILLIS: Long = (1000 / GAMEPAD_POLLING_RATE_PER_SECOND)

const val GAMEPAD_JOYSTICK_DEADZONE: Short = 2000

// FIXME: finish implementing controller input
actual suspend fun pollGamepad() = withContext(Dispatchers.IO) {
    val controller = getGamepadSDL()

    val event = SDL_Event()
    while (true) {
        if (SDL_WaitEvent(event) == 0) {
            val message = "Error occurred while waiting for SDL Event: ${SdlError.SDL_GetError()}"
            throw RuntimeException(message)
        }

//        SDL_GameControllerRumble(controller, Short.MAX_VALUE, Short.MAX_VALUE, 100)
        when (event.type) {
            SDL_EventType.SDL_CONTROLLERBUTTONDOWN -> processSDLControllerButton(event)
            SDL_EventType.SDL_CONTROLLERBUTTONUP -> processSDLControllerButton(event)
            SDL_EventType.SDL_CONTROLLERAXISMOTION -> processSDLControllerAxisMotion(event)

            SDL_EventType.SDL_QUIT -> break
        }

        // TODO: maybe delay or yield to avoid unintentionally hogging all the CPU time
        // delay(GAMEPAD_POLLING_DELAY_MILLIS)
    }

    SDL_Quit()
}

private fun getGamepadSDL(): SDL_GameController? {
    val result = Sdl.SDL_Init(SdlSubSystemConst.SDL_INIT_GAMECONTROLLER)
    if (result != 0) {
        throw RuntimeException("SDL initialization error code: $result")
    }

    val numJoysticks = SDL_NumJoysticks()
    if (numJoysticks < 0) {
        throw RuntimeException("Error calling SDL_NumJoysticks(): ${SdlError.SDL_GetError()}")
    }


    for (i in 0..numJoysticks) {
        if (SDL_IsGameController(i)) {
            return SDL_GameControllerOpen(i)
        }
    }

    Logger.DEFAULT.log("there are no gamepads connected")
    return null
}

private var leftX: Short = 0
private var leftY: Short = 0
private var rightX: Short = 0
private var rightY: Short = 0
private fun processSDLControllerAxisMotion(event: SDL_Event) {
    val axisID = event.caxis.axis
    val axisValue = event.caxis.value
    val axisName = SDL_GameControllerGetStringForAxis(axisID.toInt())

    when (axisID) {
        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERLEFT.toByte() -> {}
        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERRIGHT.toByte() -> {}

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTX.toByte() -> {
            leftX = axisValue
            Gamepad.leftJoystickCallback(leftY, leftY)
        }

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTY.toByte() -> {
            leftY = axisValue
            Gamepad.leftJoystickCallback(leftX, leftY)
        }

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTX.toByte() -> {
            rightX = axisValue
            Gamepad.rightJoystickCallback(rightX, rightY)
        }

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTY.toByte() -> {
            rightY = axisValue
            Gamepad.rightJoystickCallback(rightX, rightY)
        }

        else -> {}
    }

    Logger.DEFAULT.log("controller axis [$axisValue]: $axisName ($axisID)")
}

private fun processSDLControllerButton(event: SDL_Event) {
    val buttonID = event.cbutton.button
    val state = when (event.cbutton.state) {
        SdlEventsConst.SDL_PRESSED -> "down"
        SdlEventsConst.SDL_RELEASED -> "up"
        else -> "unknown state"
    }

    val isButtonPressed = when (val buttonState = event.cbutton.state) {
        SdlEventsConst.SDL_PRESSED -> true
        SdlEventsConst.SDL_RELEASED -> false
        else -> {
            // I guess we can default to false, but if this code path is actually used, that's not good
            Logger.DEFAULT.log("Warning: SDL controller button state is unknown: $buttonState")
            false
        }
    }

    val name = when (buttonID) {
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A.toByte() -> {
            Gamepad.onButtonA(isButtonPressed)
            "A"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_B.toByte() -> {
            Gamepad.onButtonB(isButtonPressed)
            "B"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_X.toByte() -> {
            Gamepad.onButtonX(isButtonPressed)
            "X"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_Y.toByte() -> {
            Gamepad.onButtonY(isButtonPressed)
            "Y"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_UP.toByte() -> "DPAD UP"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_DOWN.toByte() -> "DPAD DOWN"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_LEFT.toByte() -> "DPAD LEFT"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_RIGHT.toByte() -> "DPAD RIGHT"

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_LEFTSTICK.toByte() -> "LEFT STICK"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_RIGHTSTICK.toByte() -> "RIGHT STICK"

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_LEFTSHOULDER.toByte() -> "LEFT SHOULDER"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER.toByte() -> "RIGHT SHOULDER"

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_BACK.toByte() -> "LEFT MENU BUTTON (2 SQUARES)"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_GUIDE.toByte() -> "XBOX BUTTON"
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_START.toByte() -> "RIGHT MENU BUTTON"

        else -> "Unknown"
    }

    Logger.DEFAULT.log("controller button [$state]: $name ($buttonID)")
}
