package me.arianb.storm_robot

import kotlinx.serialization.Serializable

@Serializable
data class WheelMovementPacket(
    val x: Byte,
    val y: Byte,
    val rotation: Byte
)

@Serializable
data class ArmMovementPacket(
    val baseAngle: UByte,
    val shoulderAngle: UByte,
    val elbowAngle: UByte,
    val wristAngle: UByte,
    val gripperAngle: UByte
)
