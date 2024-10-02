package me.arianb.storm_robot

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException


object CameraFeed {
    const val RETRY_WAIT_TIME_MILLIS: Long = 2500

    val frameChannel: Channel<ImageBitmap> = Channel(
        capacity = CAMERA.EXPECTED_FPS,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val client = HttpClient {
        install(WebSockets) {
            pingInterval = SERVER.PING_PERIOD_MILLIS
            maxFrameSize = Long.MAX_VALUE
//            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Logging)
    }

    suspend fun start(): CloseReason? {
        var returnCloseReason: CloseReason? = null
        client.webSocket(
            host = SERVER.HOST,
            port = SERVER.PORT,
            path = "/video"
        ) {
            try {
                for (frame in incoming) {
                    frame as? Frame.Binary ?: continue
                    val frameBytes = frame.data
//                    println("CLIENT RECEIVED ${frameBytes.size} BYTES")

                    val imageBitmap = bytesToImageBitmap(frameBytes)
                    frameChannel.send(imageBitmap)
                }
            } catch (e: ClosedReceiveChannelException) {
                Logger.DEFAULT.log(
                    "server disconnected from camera websocket, reason: ${closeReason.await()}"
                )
            } catch (e: Throwable) {
                Logger.DEFAULT.log("onError ${closeReason.await()}")
                Logger.DEFAULT.log(e.stackTraceToString())
            } finally {
                returnCloseReason = closeReason.await()
                close()
            }
        }
        return returnCloseReason
    }

}

expect fun bytesToImageBitmap(frameBytes: ByteArray): ImageBitmap
