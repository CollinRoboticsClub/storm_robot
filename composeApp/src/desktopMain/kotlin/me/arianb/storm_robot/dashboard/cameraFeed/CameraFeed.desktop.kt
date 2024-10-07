package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun bytesToImageBitmap(frameBytes: ByteArray) =
    Image.makeFromEncoded(frameBytes).toComposeImageBitmap()
