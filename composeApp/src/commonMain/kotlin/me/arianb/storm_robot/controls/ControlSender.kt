package me.arianb.storm_robot.controls

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import me.arianb.storm_robot.Server

// This is where the actual implementation of something like "move robot forward" would be.

// FIXME: implement
object ControlSender {
    private val client = HttpClient {
        install(WebSockets) {
            pingIntervalMillis = Server.PING_PERIOD_MILLIS
            maxFrameSize = Long.MAX_VALUE
//            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Logging)
    }

    suspend fun start() {

    }


    fun sendMovement() {

    }
}
