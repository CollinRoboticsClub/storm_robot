package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import me.arianb.storm_robot.Camera
import me.arianb.storm_robot.Server
import me.arianb.storm_robot.applyCommonHttpClientConfig
import me.arianb.storm_robot.websocketCatching

object CameraFeed {
    var serverHost = Server.HOST
    var serverPort = Server.PORT

    val frameChannel: Channel<ImageBitmap> = Channel(
        capacity = Camera.EXPECTED_FPS,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val client = HttpClient {
        applyCommonHttpClientConfig()
    }

    suspend fun start(onConnectionError: (Throwable) -> Unit) {
        client.websocketCatching(
            host = this.serverHost,
            port = this.serverPort,
            path = Server.Endpoints.VIDEO,
            onConnectionError = onConnectionError,
        ) {
            for (frame in incoming) {
                frame as? Frame.Binary ?: continue
                val frameBytes = frame.data
//                    println("CLIENT RECEIVED ${frameBytes.size} BYTES")

                val imageBitmap = bytesToImageBitmap(frameBytes)
                frameChannel.send(imageBitmap)
            }
        }
    }
}

expect fun bytesToImageBitmap(frameBytes: ByteArray): ImageBitmap
