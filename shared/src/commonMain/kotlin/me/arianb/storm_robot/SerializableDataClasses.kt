package me.arianb.storm_robot

import kotlinx.serialization.Serializable

@Serializable
data class WheelMovementPacket(
    val x: Float,
    val y: Float,
    val rotation: Float
)

@Serializable
data class ArmMovementPacket(
    val baseAngle: Int,
    val shoulderAngle: Int,
    val elbowAngle: Int,
    val wristAngle: Int,
    val gripperAngle: Int
)
