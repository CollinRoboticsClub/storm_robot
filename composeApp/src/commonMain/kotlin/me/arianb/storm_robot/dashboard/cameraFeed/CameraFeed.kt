package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import me.arianb.storm_robot.Camera
import me.arianb.storm_robot.Server
import me.arianb.storm_robot.websocketCatching

class CameraFeed(val id: Int) {
    val frameChannel: Channel<ImageBitmap> = Channel(
        capacity = Camera.EXPECTED_FPS,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun start(client: HttpClient, host: String, port: Int, onConnectionError: (Throwable) -> Unit) {
        client.websocketCatching(
            host = host,
            port = port,
//            path = Server.Endpoints.VIDEO + "/${this.id}", // FIXME: implement
            path = Server.Endpoints.VIDEO,
            onConnectionError = onConnectionError,
        ) {
            for (frame in incoming) {
                frame as? Frame.Binary ?: continue
                val frameBytes = frame.data
//                    println("CLIENT RECEIVED ${frameBytes.size} BYTES")

                val imageBitmap = runCatching { bytesToImageBitmap(frameBytes) }.getOrNull() ?: run {
//                    Logger.w("Failed to load image: ${frameBytes.toHexString()}")
                    Logger.w("Failed to load image: ${frameBytes.size} bytes")
                    continue
                }

                frameChannel.send(imageBitmap)
            }
        }
    }
}

expect fun bytesToImageBitmap(frameBytes: ByteArray): ImageBitmap
