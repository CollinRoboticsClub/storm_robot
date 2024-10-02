package me.arianb.storm_robot.arm

// NOTE: worst-case scenario, I can run an extremely stripped down python script with just fastAPI
//       (or even something more minimal) and the libraries that interface with the servo controller

// FIXME: implement
//from adafruit_motor.servo import Servo
class Servo {
    var angle: Int = 0
}

// Python version: https://github.com/adafruit/Adafruit_CircuitPython_ServoKit/blob/1ca2cdd800744fe70daba49688ee4cdef414d928/adafruit_servokit.py
// FIXME: implement
//from adafruit_servokit import ServoKit
class ServoKit(val channels: Int) {
    val servo = arrayOf(
        Servo(),
        Servo(),
        Servo(),
        Servo(),
        Servo(),
        Servo(),
    )
}
