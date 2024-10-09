package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.arianb.storm_robot.CAMERA
import me.arianb.storm_robot.SERVER


object CameraFeed {
    const val RETRY_WAIT_TIME_MILLIS: Long = 2500

    var serverHost = SERVER.HOST
    var serverPort = SERVER.PORT

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

    suspend fun start() {
        client.webSocket(
            host = this.serverHost,
            port = this.serverPort,
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
            } catch (e: CancellationException) {
                Logger.DEFAULT.log("CancellationException")

                // Propagate the cancellation
                ensureActive()

                Logger.DEFAULT.log("CancellationException: we didn't re-throw it")
            } catch (e: Throwable) {
                Logger.DEFAULT.log("onError ${closeReason.await()}")
                Logger.DEFAULT.log(e.stackTraceToString())
            } finally {
                close()
            }
        }
    }
}

class RestartableJob(
    private val coroutineScope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> Unit,
    private val handler: CoroutineExceptionHandler
) {
    private var job: Job = Job()

    fun start(): Job =
        coroutineScope.launch {
            supervisorScope {
                job = launch(handler, block = block)
            }
        }

    fun restart() {
        coroutineScope.launch {
            stop().join()
            start()
        }
    }

    fun stop() =
        coroutineScope.launch {
            supervisorScope {
                job.cancelAndJoin()
            }
        }
}

expect fun bytesToImageBitmap(frameBytes: ByteArray): ImageBitmap
