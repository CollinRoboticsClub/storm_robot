package me.arianb.storm_robot.controls

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.serialization.json.Json
import me.arianb.storm_robot.Server
import me.arianb.storm_robot.WheelMovementPacket
import me.arianb.storm_robot.applyCommonHttpClientConfig
import me.arianb.storm_robot.websocketCatching

// This is where the actual implementation of something like "move robot forward" would be.
// More specifically, this is where the code to "tell the server how to move the robot" is written.
object ControlSender {
    // TODO: decide if this onBufferOverflow behavior is a good idea. I'm not sure about it right now.
    private val messageChannel: Channel<Any> = Channel(1000, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val client = HttpClient {
        applyCommonHttpClientConfig()
        WebSockets {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    suspend fun start(onConnectionError: (Throwable) -> Unit, onErrorInBlock: (Throwable) -> Unit) {
        client.websocketCatching(
            host = Server.HOST,
            port = Server.PORT,
            path = with(Server.Endpoints) {
                API_ROOT + API_WHEELS + "/move"
            },
            onConnectionError = onConnectionError,
            onErrorInBlock = onErrorInBlock
        ) {
            for (message in messageChannel) {
                runCatching {
                    sendSerialized(message)
                }.onFailure { throwable ->
                    Logger.e("exception thrown while attempted to serialize and send message: $throwable")
                }
            }
        }
    }

    fun sendMovement(x: Short, y: Short) {
        // FIXME: replace placeholder rotation value
//        val rotation: Short = ???

        val wheelMovementPacket = WheelMovementPacket(
            x.mapToByte(),
            y.mapToByte(),
//            rotation.mapToByte(),
            0
        )
        Logger.d("packet: $wheelMovementPacket")

        messageChannel.trySend(
            wheelMovementPacket
        ).onFailure {
            Logger.e("uh oh, we failed to add a WheelMovementPacket to the channel.")
        }
    }
}

fun Short.mapToByte(): Byte =
    mapOntoRange(fromRange = Pair(Short.MIN_VALUE, Short.MAX_VALUE), toRange = Pair(Byte.MIN_VALUE, Byte.MAX_VALUE))

fun Short.mapOntoRange(fromRange: Pair<Short, Short>, toRange: Pair<Byte, Byte>): Byte =
    ((this - fromRange.first) *
            (toRange.second - toRange.first) / (fromRange.second - fromRange.first)
            + toRange.first
            ).toByte()