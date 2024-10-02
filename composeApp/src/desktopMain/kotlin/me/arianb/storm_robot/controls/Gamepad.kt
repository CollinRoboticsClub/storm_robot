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
import io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_GameControllerOpen
import io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.SDL_IsGameController
import io.github.libsdl4j.api.joystick.SdlJoystick.SDL_NumJoysticks
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


const val GAMEPAD_POLLING_RATE_PER_SECOND: Long = 20
const val GAMEPAD_POLLING_DELAY_MILLIS: Long = (1000 / GAMEPAD_POLLING_RATE_PER_SECOND)

// FIXME: implement controller input
// FIXME: add platform-specific rumble method? for multi-platform rumble support?
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

private fun processSDLControllerAxisMotion(event: SDL_Event) {
    val axisID = event.caxis.axis
    val value = event.caxis.value

    val axisName = when (axisID) {
        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERLEFT.toByte() -> "LEFT TRIGGER"
        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERRIGHT.toByte() -> "RIGHT TRIGGER"

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTX.toByte() -> {
            Gamepad.leftJoystickCallback(value)
            "LEFT X"
        }

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTY.toByte() -> {
            Gamepad.leftJoystickCallback(value)
            "LEFT Y"
        }

        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTX.toByte() -> "RIGHT X"
        SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTY.toByte() -> "RIGHT Y"
        else -> "Unknown"
    }

    println("controller axis [$value]: $axisName ($axisID)")
}

private fun processSDLControllerButton(event: SDL_Event) {
    val buttonID = event.cbutton.button
    val state = when (event.cbutton.state) {
        SdlEventsConst.SDL_PRESSED -> "down"
        SdlEventsConst.SDL_RELEASED -> "up"
        else -> "unknown state"
    }

    val name = when (buttonID) {
        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A.toByte() -> {
            Gamepad.onButtonA()
            "A"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_B.toByte() -> {
            Gamepad.onButtonB()
            "B"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_X.toByte() -> {
            Gamepad.onButtonX()
            "X"
        }

        SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_Y.toByte() -> {
            Gamepad.onButtonY()
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

    println("controller button [$state]: $name ($buttonID)")
}
