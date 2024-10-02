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
            shoulder.setAngle(90)
            elbow.setAngle(135)
            wrist.setAngle(135)
            //gripper.setAngle(0)
        }
    }
}

class SafeServo(
    private val servo: Servo,
    private val minAngle: Int,
    private val maxAngle: Int,
    initialAngle: Int
) {
    init {
        servo.angle = initialAngle
    }

    fun setAngle(absoluteAngle: Int) {
        // Clamp values to valid range
        if (absoluteAngle > maxAngle || absoluteAngle < minAngle) {
            // println("provided angle is out of range, ignoring it")
            return
        }

        // Don't do anything if servo angle is already correct
        if (absoluteAngle == servo.angle) {
            // println("servo is already at this angle, ignoring it")
            return
        }

        servo.angle = absoluteAngle
    }

    fun move(relativeAngle: Int) {
        val absoluteAngle = servo.angle + relativeAngle

        return setAngle(absoluteAngle)
    }
}

object Arm {
    private val servoList: Array<Servo>
    val base: SafeServo
    val shoulder: SafeServo
    val elbow: SafeServo
    val wrist: SafeServo
    val gripper: SafeServo

    init {
        val kit = ServoKit(channels = 16)

        // PIN-OUT
        base = SafeServo(kit.servo[0], initialAngle = 90, minAngle = 70, maxAngle = 110)
        shoulder = SafeServo(kit.servo[1], initialAngle = 90, minAngle = 40, maxAngle = 180)
        elbow = SafeServo(kit.servo[2], initialAngle = 90, minAngle = 20, maxAngle = 135)
        wrist = SafeServo(kit.servo[3], initialAngle = 90, minAngle = 70, maxAngle = 110)
        gripper = SafeServo(kit.servo[4], initialAngle = 90, minAngle = 70, maxAngle = 150)

        servoList = kit.servo
    }
}
