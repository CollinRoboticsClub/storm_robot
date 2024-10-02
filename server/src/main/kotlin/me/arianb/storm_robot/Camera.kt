package me.arianb.storm_robot

import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamUtils
import io.ktor.server.application.log
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withContext
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

const val FRAME_FORMAT = "jpeg"

fun Route.cameraRoutes() {
    webSocket {
        withContext(Dispatchers.IO) {
            val webcam = getWebcam()

            try {
//                var counter = 0
                webcam?.open() ?: throw Throwable("failed to open webcam :(")

                while (true) {
//                    val thisFrame = FrameGeneration.getFrame(counter)
                    val thisFrame = WebcamUtils.getImageBytes(webcam, FRAME_FORMAT)

                    val result = outgoing.trySend(Frame.Binary(true, thisFrame))
                    if (result.isClosed) {
                        application.log.warn(
                            "Failed to send camera frame, Channel was closed. Client probably just abruptly closed the connection. Exception: {}",
                            result.exceptionOrNull()?.message
                        )
                        break
                    }

//                    println(counter++)
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
                close()
                webcam?.close()
            }
        }
    }
}

fun getWebcam(): Webcam? {
    val webcam = Webcam.getDefault()?.apply {
        val newDimension = Dimension(CAMERA.RESOLUTION_WIDTH, CAMERA.RESOLUTION_HEIGHT)
        setCustomViewSizes(newDimension)
        viewSize = newDimension
    }

    // Apparently this webcam library keeps its webcam discovery service running after the
    // initial discovery, but I don't want that, so I'm stopping it.
    Webcam.getDiscoveryServiceRef()?.stop()

    return webcam
}

// Some code for generating and caching some basic images with an integer in the center.
// The purpose of this was to allow me to test streaming performance while minimizing the number of
// things that could possibly be impacting performance.
object FrameGeneration {
    private val generatedFrames = mutableListOf<ByteArray>()
    private val outputStream = ByteArrayOutputStream()
    private val img =
        BufferedImage(CAMERA.RESOLUTION_WIDTH, CAMERA.RESOLUTION_HEIGHT, BufferedImage.TYPE_INT_RGB)
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
