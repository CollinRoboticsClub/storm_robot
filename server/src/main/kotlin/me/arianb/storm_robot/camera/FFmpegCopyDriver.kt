package me.arianb.storm_robot.camera

import com.github.sarxos.webcam.WebcamDevice
import com.github.sarxos.webcam.WebcamDriver
import com.github.sarxos.webcam.WebcamException
import org.bridj.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStreamReader

private class VideoDeviceFilenameFilter : FilenameFilter {
    override fun accept(dir: File, name: String): Boolean {
        return (dir.name == "dev" &&
                name.startsWith("video") &&
                Character.isDigit(name[5]))
    }

    val videoFiles: List<File>
        get() {
            return buildList {
                val names = DEV.list(this@VideoDeviceFilenameFilter)
                for (i in names.indices) {
                    add(File(DEV, names[i]))
                }
            }
        }

    companion object {
        private val DEV = File("/dev")
    }
}

class FFmpegCopyDriver : WebcamDriver {
    private var path: String = ""

    private val command: String
        get() = getCommand(path)

    override fun getDevices(): List<WebcamDevice> {
        if (!Platform.isUnix()) {
            throw UnsupportedOperationException("Unsupported Platform")
        }

        return findUnixDevices()
    }

    private fun findUnixDevices(): List<WebcamDevice> {
        return buildList<WebcamDevice> {
            for (videoFile in VFFILTER.videoFiles) {
                val cmd = arrayOf<String>(
                    this@FFmpegCopyDriver.command,
                    "-f", captureDriver,
                    "-hide_banner", "",
                    "-list_formats", "all",
                    "-i", videoFile.absolutePath,
                )

                val process = startProcess(cmd)
                if (process == null) {
                    TODO("fix NPE :(")
                }

                val starter = "[" + captureDriver
                val marker = "] Raw"
                try {
                    process.inputStream.use { inStream ->
                        InputStreamReader(inStream).use { inStreamReader ->
                            BufferedReader(inStreamReader).useLines { lines ->
                                val line = lines.find { line ->
                                    line.startsWith(starter) && line.contains(marker)
                                }
                                if (line != null) {
                                    LOG.debug("Command stdout line: {}", line)
                                    val resolutions = line.split(" : ".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()[3].trim { it <= ' ' }
                                    add(FFmpegCopyDevice(path, videoFile, resolutions))
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    throw WebcamException(e)
                } finally {
                    process.destroy()
                    try {
                        process.waitFor()
                    } catch (e: InterruptedException) {
                        throw WebcamException(e)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun startProcess(cmd: Array<String>): Process? {
        if (true /*LOG.isDebugEnabled*/) {
            val sb = StringBuilder()
            for (c in cmd) {
                sb.append(c).append(' ')
            }
            LOG.debug("Executing command: {}", sb.toString())
        }

        var process: Process? = null
        try {
            val builder = ProcessBuilder(*cmd)
                .redirectErrorStream(true)
            process = builder.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        process?.outputStream?.close()

        return process
    }

    override fun isThreadSafe(): Boolean {
        return false
    }

    override fun toString(): String {
        return javaClass.simpleName
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FFmpegCopyDriver::class.java)

        private val VFFILTER: VideoDeviceFilenameFilter = VideoDeviceFilenameFilter()

        val captureDriver: String by lazy { detectCaptureDriver() }

        private fun detectCaptureDriver(): String {
            if (Platform.isLinux()) {
                return "video4linux2"
            } else if (Platform.isWindows()) {
                return "dshow"
            } else if (Platform.isMacOSX()) {
                return "avfoundation"
            }

            // Platform not supported
            throw UnsupportedOperationException("Unsupported platform")
        }

        fun getCommand(path: String): String {
            return path + "ffmpeg"
        }
    }
}
