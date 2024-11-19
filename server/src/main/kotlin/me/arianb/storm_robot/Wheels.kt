package me.arianb.storm_robot

import io.ktor.serialization.deserialize
import io.ktor.server.application.log
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import kotlinx.coroutines.channels.ClosedReceiveChannelException

fun Route.wheelsRoutes() {
    webSocket("/move") {
        try {
            for (frame in incoming) {
                val movementPacket = converter?.deserialize<WheelMovementPacket>(frame)
                    ?: throw RuntimeException("failed to deserialize WheelMovementPacket, websocket converter is null")
                application.log.debug("received: {}", movementPacket)

                MicrocontrollerSerialConnection.send(movementPacket)
            }
        } catch (e: ClosedReceiveChannelException) {
            // ensure motors are stopped on client disconnection
            MicrocontrollerSerialConnection.send(WheelMovementPacket(0, 0, 0))

            application.log.info(
                "client disconnected from wheels websocket, reason: {}",
                closeReason.await()
            )
        } catch (e: Throwable) {
            application.log.error("onError {}", closeReason.await())
            application.log.error(e)
        }
    }
}

