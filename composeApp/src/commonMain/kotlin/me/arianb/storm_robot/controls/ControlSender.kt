package me.arianb.storm_robot.controls

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.serialization.json.Json
import me.arianb.storm_robot.Server
import me.arianb.storm_robot.WheelMovementPacket

// This is where the actual implementation of something like "move robot forward" would be. More specifically, this is
// where the code to "tell the server how to move the robot" is written.

// FIXME: finish implementation
object ControlSender {
    // TODO: decide if this onBufferOverflow behavior is a good idea. I'm not sure about it right now.
    private val messageChannel: Channel<Any> = Channel(1000, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val client = HttpClient {
        install(WebSockets) {
            pingIntervalMillis = Server.PING_PERIOD_MILLIS
            maxFrameSize = Long.MAX_VALUE
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Logging)
    }

    suspend fun start() {
        client.webSocket(
            host = Server.HOST,
            port = Server.PORT,
            path = with(Server.Endpoints) {
                API_ROOT + API_WHEELS + "/move"
            }
        ) {
            for (message in messageChannel) {
                runCatching {
                    sendSerialized(message)
                }.onFailure { throwable ->
                    Logger.DEFAULT.log("exception thrown while attempted to serialize and send message: $throwable")
                }
            }
        }
    }

    fun sendMovement(x: Short, y: Short) {
        // FIXME: replace placeholder rotation value
        val rotation: Short = 0

        messageChannel.trySend(
            WheelMovementPacket(
                x.mapToByte(),
                y.mapToByte(),
                rotation.mapToByte()
            )
        ).onFailure {
            Logger.DEFAULT.log("uh oh, we failed to add a WheelMovementPacket to the channel.")
        }
    }
}

fun Short.mapToByte(): Byte =
    mapOntoRange(fromRange = Pair(Short.MIN_VALUE, Short.MAX_VALUE), toRange = Pair(Byte.MIN_VALUE, Byte.MAX_VALUE))

fun Short.mapOntoRange(fromRange: Pair<Short, Short>, toRange: Pair<Byte, Byte>): Byte =
    (toRange.first + (
            (toRange.second - toRange.first) / (fromRange.second - fromRange.first)
            ) * (this - fromRange.first)
            ).toByte()