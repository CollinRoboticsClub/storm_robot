package me.arianb.storm_robot

import co.touchlab.kermit.Logger
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamException
import com.github.sarxos.webcam.WebcamUtils
import io.ktor.server.application.log
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.util.getValue
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import io.ktor.util.moveToByteArray
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import me.arianb.storm_robot.camera.MjpegInputStream
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds

const val FRAME_FORMAT = "jpeg"

fun Route.cameraRoutes() {
    route("/info") {
        getAvailableWebcamsInfo()
    }

    streamWebcam()
}

private fun Route.getAvailableWebcamsInfo() = get {
    val availableWebcams = WebcamConnections.getAvailableWebcamIdentifiers()
    Logger.i { "Available webcams: $availableWebcams" }
    call.respond(availableWebcams)
}

// OPTIMIZEME: the ideas now are:
//  1. continue trying to optimize mjpeg copy latency
//  2. switch to h264 encoding for hardware support on the Pi 4
private fun Route.streamWebcam() = webSocket("{id}") {
    return@webSocket withContext(Dispatchers.Default) {
        val id: Int by call.parameters

        try {
            val webcam: Webcam = WebcamConnections.getWebcam(id)

            val profilingThing = MeasureCountPerTime(1.seconds)
            WebcamFrameFlows.getWebcamFramesFlow(webcam).collect { frameBytes ->
                val result = outgoing.trySend(Frame.Binary(true, frameBytes))

                profilingThing.check()
                println(profilingThing.currentCount)

                if (result.isClosed) {
                    application.log.warn(
                        "Failed to send camera frame, Channel was closed. Client probably just abruptly closed the connection. Exception: {}",
                        result.exceptionOrNull()?.message
                    )
                    this.coroutineContext.job.cancel()
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            application.log.info(
                "client disconnected from camera websocket, reason: {}",
                closeReason.await()
            )
        } catch (e: Throwable) {
            application.log.error("onError {}", closeReason.await())
            application.log.error(e)
        } finally {
            application.log.warn("server is closing camera websocket")
            close()
        }
    }
}

private object WebcamFrameFlows {
    fun getWebcamFramesFlow(webcam: Webcam): Flow<ByteArray> =
        getWebcamFramesFlowWebcamUtils(webcam)

    fun getWebcamFramesFlowFrameGen(): Flow<ByteArray> {
        return flow<ByteArray> {
            var counter = 0
            while (true) {
                val thisFrameJpegBytes = FrameGeneration.getFrame(counter++)
                emit(thisFrameJpegBytes)
            }
        }
    }

    fun getWebcamFramesFlowMjpegRawBuffer(webcam: Webcam): Flow<ByteArray> {
        // NOTE: Re-using existing ByteBuffer is currently broken for some reason, haven't troubleshooted it much yet.
        val jpegFrameBuffer = ByteBuffer.allocate(MjpegInputStream.FRAME_MAX_LENGTH)

        return flow<ByteArray> {
            while (true) {
                webcam.getImageBytes(jpegFrameBuffer)
                val frameBytes = jpegFrameBuffer.moveToByteArray()
                emit(frameBytes)
                jpegFrameBuffer.reset()
            }
        }
    }

    fun getWebcamFramesFlowMjpegRaw(webcam: Webcam): Flow<ByteArray> {
        return flow<ByteArray> {
            while (true) {
                val frameByteBuffer = webcam.imageBytes
                val frameBytes = frameByteBuffer.moveToByteArray()

                emit(frameBytes)
            }
        }
    }

    fun getWebcamFramesFlowWebcamUtils(webcam: Webcam): Flow<ByteArray> {
        ImageIO.setUseCache(false)

        return flow<ByteArray> {
            while (true) {
                val thisFrameBytes = WebcamUtils.getImageBytes(webcam, FRAME_FORMAT)

                emit(thisFrameBytes)
            }
        }
    }
}

object WebcamConnections {
    private var openWebcams: Map<Int, Webcam> = emptyMap()

    init {
        Webcam.getDiscoveryService().setEnabled(false)
//        Webcam.setDriver(FFmpegCopyDriver())
    }

    private fun getAvailableWebcamDevices(): List<Webcam> {
        // Refresh list of available webcams (Remove disconnected ones, add newly connected ones)
        Webcam.getDiscoveryService().scan()

        val availableWebcams: List<Webcam> = Webcam.getWebcams() ?: emptyList()

        // Apparently this webcam library keeps its webcam discovery service running after the
        // initial discovery, but I don't want that, so I'm stopping it.
        Webcam.getDiscoveryServiceRef()?.stop()

        return availableWebcams
    }

    fun getAvailableWebcamIdentifiers(): List<WebcamIdentifier> {
        return getAvailableWebcamDevices().mapIndexed { index, it ->
            WebcamIdentifier(index, it.name ?: "null")
        }
    }

    /**
     * @throws WebcamException
     */
    fun getWebcam(id: Int): Webcam {
        val openWebcam: Webcam = openWebcams.getOrElse(id) {
            val thisWebcam: Webcam? = getAvailableWebcamDevices().getOrNull(id)
            if (thisWebcam == null) {
                throw WebcamException("Failed to acquire webcam")
            }

            if (!thisWebcam.isOpen) {
                thisWebcam.apply {
                    val newDimension = Dimension(Camera.RESOLUTION_WIDTH, Camera.RESOLUTION_HEIGHT)
                    setCustomViewSizes(newDimension)
                    viewSize = newDimension
                }

                if (!thisWebcam.open()) {
                    throw WebcamException("Failed to open webcam")
                }
            }

            thisWebcam
        }

        return openWebcam
    }
}

// Some code for generating and caching some basic images with an integer in the center.
// The purpose of this was to allow me to test streaming performance while minimizing the number of
// things that could possibly be impacting performance.
private object FrameGeneration {
    private val generatedFrames = mutableListOf<ByteArray>()
    private val outputStream = ByteArrayOutputStream()
    private val img = BufferedImage(Camera.RESOLUTION_WIDTH, Camera.RESOLUTION_HEIGHT, BufferedImage.TYPE_INT_RGB)
    private val g2d: Graphics2D = img.createGraphics()

    fun getFrame(index: Int): ByteArray {
        val thisFrame = generatedFrames.getOrNull(index)
        if (thisFrame == null) {
            generateFrame(index).let {
                generatedFrames.add(index, it)
                return it
            }
        } else {
            return thisFrame
        }
    }

    private fun generateFrame(
        num: Int,
    ): ByteArray {
        with(g2d) {
            color = Color.RED
            fillRect(0, 0, img.width, img.height)
            color = Color.BLACK
            font = font.deriveFont(75f)
            drawString(num.toString(), img.width / 2, img.height / 2)
        }

        outputStream.reset()
        ImageIO.write(img, FRAME_FORMAT, outputStream)
        val bytes = outputStream.toByteArray()

        return bytes
    }
}
