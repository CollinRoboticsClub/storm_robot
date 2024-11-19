package me.arianb.storm_robot.arm

import io.ktor.serialization.deserialize
import io.ktor.server.application.log
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import me.arianb.storm_robot.ArmMovementPacket

fun Route.armRoutes() {
    webSocket("/move") {
        try {
            for (frame in incoming) {
                val movementPacket = converter?.deserialize<ArmMovementPacket>(frame)
                    ?: throw RuntimeException("failed to deserialize ArmMovementPacket, websocket converter is null")
                application.log.debug("received: {}", movementPacket)

                movementPacket.let {
                    with(Arm) {
                        base.move(it.baseAngle)
                        shoulder.move(it.shoulderAngle)
                        elbow.move(it.elbowAngle)
                        wrist.move(it.wristAngle)
                        gripper.move(it.gripperAngle)
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            application.log.info(
                "client disconnected from arm websocket, reason: {}",
                closeReason.await()
            )
        } catch (e: Throwable) {
            application.log.error("onError {}", closeReason.await())
            application.log.error(e)
        }
    }

    post("/preset/default") {
        with(Arm) {
            //base.setAngle(0)
            shoulder.setAngle(90u)
            elbow.setAngle(135u)
            wrist.setAngle(135u)
            //gripper.setAngle(0)
        }
    }
}

typealias ServoAngle = UByte

class Servo(
    initialAngle: ServoAngle,
    private val minAngle: ServoAngle,
    private val maxAngle: ServoAngle,
) {
    private var angle: ServoAngle = initialAngle

    fun setAngle(absoluteAngle: ServoAngle) {
        // Clamp values to valid range
        if (absoluteAngle > maxAngle || absoluteAngle < minAngle) {
            // println("provided angle is out of range, ignoring it")
            return
        }

        // Don't do anything if servo angle is already correct
        if (absoluteAngle == angle) {
            // println("servo is already at this angle, ignoring it")
            return
        }

        angle = absoluteAngle
    }

    fun move(relativeAngle: ServoAngle) {
        val absoluteAngle: ServoAngle = (angle + relativeAngle).toUByte()

        return setAngle(absoluteAngle)
    }
}

object Arm {
    private val servoList: List<Servo> = mutableListOf()
    val base: Servo
    val shoulder: Servo
    val elbow: Servo
    val wrist: Servo
    val gripper: Servo

    init {
        // PIN-OUT
        base = Servo(initialAngle = 90u, minAngle = 70u, maxAngle = 110u)
        shoulder = Servo(initialAngle = 90u, minAngle = 40u, maxAngle = 180u)
        elbow = Servo(initialAngle = 90u, minAngle = 20u, maxAngle = 135u)
        wrist = Servo(initialAngle = 90u, minAngle = 70u, maxAngle = 110u)
        gripper = Servo(initialAngle = 90u, minAngle = 70u, maxAngle = 150u)
    }
}
