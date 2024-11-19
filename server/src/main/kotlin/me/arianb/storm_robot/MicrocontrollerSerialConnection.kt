package me.arianb.storm_robot

import com.fazecast.jSerialComm.SerialPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

object MicrocontrollerSerialConnection : Closeable {
    private const val DEVICE_PATH: String = "/dev/ttyUSB0"
    private const val BAUD_RATE: Int = 9600
    private const val TIMEOUT_MILLIS: Int = 100
    private const val SERIAL_MESSAGE_SEPARATOR: Byte = ' '.code.toByte()

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
            byteArrayOf(
                it.x,
                it.y,
                it.rotation,
                SERIAL_MESSAGE_SEPARATOR
            )
        }

        withContext(Dispatchers.IO) {
//            println("sending wheel movement: $movementPacket")
            serialPort.writeBytes(bytes, bytes.size)
//            println("finished sending wheel movement packet")
        }
    }
}