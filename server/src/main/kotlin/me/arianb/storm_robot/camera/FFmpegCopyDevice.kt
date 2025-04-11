package me.arianb.storm_robot.camera

import com.github.sarxos.webcam.WebcamDevice
import com.github.sarxos.webcam.WebcamException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class FFmpegCopyDevice(
    private var path: String,
    private var name: String,
    resolutions: String
) : WebcamDevice, WebcamDevice.BufferAccess {

    private val open = AtomicBoolean(false)
    private val disposed = AtomicBoolean(false)

    @Volatile
    private var process: Process? = null

    private val resolutions: Array<Dimension>
    private var resolution: Dimension

    private var mjpegStream: MjpegInputStream? = null

    constructor(path: String, vfile: File, resolutions: String) : this(
        path,
        vfile.absolutePath,
        resolutions
    )

    init {
        this.resolutions = readResolutions(resolutions)
        this.resolution = getResolutions()[0]
    }

    @Throws(IOException::class)
    fun startProcess() {
        val builder = ProcessBuilder(*buildCommand())
        builder.redirectErrorStream(true) // so we can ignore the error stream

        process = builder.start()
        mjpegStream = MjpegInputStream(process!!.inputStream)
    }

    private fun readResolutions(res: String): Array<Dimension> {
        return buildList<Dimension> {
            val parts = res.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            for (part in parts) {
                val xy = part.split("x".toRegex()).dropLastWhile { it.isEmpty() }
                add(Dimension(xy[0].toInt(), xy[1].toInt()))
            }
        }.toTypedArray()
    }

    private val resolutionString: String
        get() {
            resolution.let {
                return "${it.width}x${it.height}"
            }
        }

    override fun open() {
        if (!open.compareAndSet(false, true)) {
            return
        }

        try {
            startProcess()

        } catch (e: IOException) {
            throw WebcamException(e)
        }
    }

    override fun close() {
        if (!open.compareAndSet(true, false)) {
            return
        }

        process!!.destroy()

        try {
            process!!.waitFor()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true) && isOpen) {
            close()
        }
    }

    override fun isOpen(): Boolean {
        return open.get()
    }

    fun buildCommand(): Array<String> {
        val inputFormat = "mjpeg" // FIXME: un-hardcode this
        val videoCodec = "copy"

        val outputFormat = if (videoCodec == "copy") {
            inputFormat
        } else {
            videoCodec
        }

        return arrayOf<String>(
            FFmpegCopyDriver.getCommand(this.path),
            "-loglevel", "panic",  // suppress ffmpeg headers
            "-re", // read at native frame rate
            "-s", this.resolutionString,  // frame dimension
            "-f", FFmpegCopyDriver.captureDriver,  // camera format
            "-input_format", inputFormat,
            "-i", this.name,  // input file
            "-vcodec", videoCodec,  // copy output
            "-f", outputFormat,  // raw output
            "-fps_mode", "vfr",  // avoid frame duplication
            "-",  // output to stdout
        )
    }

    override fun getName(): String = this.name

    override fun getResolutions(): Array<Dimension> = this.resolutions

    override fun getResolution(): Dimension = this.resolution

    override fun setResolution(resolution: Dimension) {
        this.resolution = resolution
    }

    @Throws(IOException::class)
    override fun getImage(): BufferedImage? {
        val stream = mjpegStream!!

        try {
            return stream.readFrame()
        } catch (e: IOException) {
            throw WebcamException("Cannot get image frame from ", e)
        }
    }

    override fun getImageBytes(): ByteBuffer? {
        if (!open.get()) {
            return null
        }

        val stream = mjpegStream!!

        return ByteBuffer.wrap(stream.readFrameBytes())
    }

    override fun getImageBytes(byteBuffer: ByteBuffer) {
        val stream = mjpegStream!!

        try {
            byteBuffer.put(stream.readFrameBytes())
        } catch (e: IOException) {
            throw WebcamException(e)
        }
    }

    private fun arraySize(): Int {
        resolution.let {
            return (it.width * it.height * 3)
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FFmpegCopyDevice::class.java)

        private fun isAlive(p: Process): Boolean {
            try {
                p.exitValue()
                return false
            } catch (e: IllegalThreadStateException) {
                return true
            }
        }
    }
}
