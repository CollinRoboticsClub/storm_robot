package me.arianb.storm_robot.camera

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * This is [InputStream] with ability to read MJPEG frames as [BufferedImage].
 *
 * @author Bartosz Firyn (sarxos)
 * @author Arian Baishya
 */
class MjpegInputStream(inputStream: InputStream) : DataInputStream(BufferedInputStream(inputStream, FRAME_MAX_LENGTH)) {
    /**
     * Is stream open?
     */
    private var open = true

    val isClosed: Boolean
        get() = !open

    @Throws(IOException::class)
    private fun getStartOfSequence(input: DataInputStream, sequence: ByteArray): Int {
        val end = getEndOfSequence(input, sequence)
        return if (end < 0) {
            -1
        } else {
            end - sequence.size
        }
    }

//    @Throws(IOException::class)
//    private fun getEndOfSequenceOld(input: DataInputStream, sequence: ByteArray): Int {
//        var s = 0
//        var c: Byte
//        for (i in 0..<FRAME_MAX_LENGTH) {
//            c = input.readByte()
//            if (c == sequence[s]) {
//                s++
//                if (s == sequence.size) {
//                    return i + 1
//                }
//            } else {
//                s = 0
//            }
//        }
//        return -1
//    }

    @Throws(IOException::class)
    private fun getEndOfSequence(input: DataInputStream, sequence: ByteArray): Int {
        val BUFFER_SIZE = 100_000

        var s = 0
        val buffer = ByteArray(BUFFER_SIZE)
        var numBytesRead = 0
        var bufferIndex = 0
        for (i in 0..<FRAME_MAX_LENGTH) {
            if (bufferIndex > numBytesRead - 1) {
                numBytesRead = input.read(buffer)
                bufferIndex = 0
            }

            val c = buffer[bufferIndex++]
            if (c == sequence[s]) {
                s++
                if (s == sequence.size) {
                    return i + 1
                }
            } else {
                s = 0
            }
        }
        return -1
    }

    /**
     * Read single MJPEG frame (JPEG image) from stream.
     *
     * @return JPEG image as [BufferedImage] or null
     * @throws IOException when there is a problem in reading from stream
     */
    @Throws(IOException::class)
    fun readFrame(): BufferedImage? {
        val frameBytes = readFrameBytes()

        return frameBytesToBufferedImage(frameBytes)
    }

    private fun frameBytesToBufferedImage(frame: ByteArray?): BufferedImage? {
        if (frame == null) {
            return null
        }

        try {
            ByteArrayInputStream(frame).use { bais ->
                return ImageIO.read(bais)
            }
        } catch (e: IOException) {
            return null
        }
    }

    @Throws(IOException::class)
    fun readFrameBytes(): ByteArray? {
        if (!open) {
            return null
        }

        mark(FRAME_MAX_LENGTH)

        // Find the start of the actual MJPEG frame
        val startIndex = getStartOfSequence(this, SOI_MARKER)
        reset()

        // Assume data that came before the start of the image within the frame is the content of a header
        val header = ByteArray(startIndex)
        readFully(header)

        // Find the start of the actual MJPEG frame
        val length = getEndOfSequence(this, EOI_MARKER)
        reset()

        if (length <= 0) {
            LOG.error("Invalid MJPEG stream, EOI (0xFF,0xD9) not found!")
        }

        val frame = ByteArray(length)

        skipBytes(startIndex)

        readFully(frame)

        return frame
    }

    @Throws(IOException::class)
    override fun close() {
        try {
            super.close()
        } finally {
            this.open = false
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(MjpegInputStream::class.java)

        /**
         * Max frame length in bytes.
         */
        const val FRAME_MAX_LENGTH: Int = 10_000_000

        /**
         * The first two bytes of every JPEG frame are the Start Of Image (SOI) marker values FFh D8h.
         */
        private val SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())

        /**
         * All JPEG data streams end with the End Of Image (EOI) marker values FFh D9h.
         */
        private val EOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
    }
}
