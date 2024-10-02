package me.arianb.storm_robot

import com.fazecast.jSerialComm.SerialPort
import io.ktor.serialization.deserialize
import io.ktor.server.application.log
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withContext
import java.io.Closeable

fun Route.wheelsRoutes() {
    webSocket("/move") {
        try {
            for (frame in incoming) {
                val movementPacket = converter?.deserialize<WheelMovementPacket>(frame)
                    ?: throw RuntimeException("failed to deserialize WheelMovementPacket, websocket converter is null")
                application.log.debug("received: {}", movementPacket)

                ArduinoSerialConnection.send(movementPacket)
            }
        } catch (e: ClosedReceiveChannelException) {
            // ensure motors are stopped on client disconnection
            ArduinoSerialConnection.send(WheelMovementPacket(0f, 0f, 0f))

            application.log.info(
                "client disconnected from wheels websocket, reason: {}",
                closeReason.await()
            )
        } catch (e: Throwable) {
            application.log.error("onError {}", closeReason.await())
            application.log.error(e)
        }
    }
}

object ArduinoSerialConnection : Closeable {
    private const val DEVICE_PATH: String = "/dev/ttyUSB0"
    private const val BAUD_RATE: Int = 9600
    private const val TIMEOUT_MILLIS: Int = 100

    private val serialPort: SerialPort

    init {
        serialPort = getSerialPort().apply {
            openPort(TIMEOUT_MILLIS)
            baudRate = BAUD_RATE
            setComPortTimeouts(
                SerialPort.TIMEOUT_WRITE_BLOCKING,
                TIMEOUT_MILLIS,
                TIMEOUT_MILLIS
            )
        }
    }

    private fun getSerialPort(): SerialPort {
        // TODO: WIP code to maybe handle when # of available serial ports != 1
//        val serialPortList: Array<SerialPort> = SerialPort.getCommPorts() ?: emptyArray()
//        if (serialPortList.isEmpty()) {
//            println("bruh there's no available serial ports")
//        } else if (serialPortList.size > 1) {
//            println("bruh there's more than one serial port")
//        } else {
//
//        }
        return SerialPort.getCommPort(DEVICE_PATH)
    }

    override fun close() {
        serialPort.closePort()
    }

    suspend fun send(movementPacket: WheelMovementPacket) {
        val bytes = movementPacket.let {
            "${it.x} ${it.y} ${it.rotation}" + "\n"
        }.toByteArray()

        withContext(Dispatchers.IO) {
//            println("sending wheel movement: $movementPacket")
            serialPort.writeBytes(bytes, bytes.size)
//            println("finished sending wheel movement packet")
        }
    }
}
