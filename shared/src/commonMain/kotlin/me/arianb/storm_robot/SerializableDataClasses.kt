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
    val baseAngle: Int,
    val shoulderAngle: Int,
    val elbowAngle: Int,
    val wristAngle: Int,
    val gripperAngle: Int
)
