package me.arianb.storm_robot

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun bytesToImageBitmap(frameBytes: ByteArray) =
    Image.makeFromEncoded(frameBytes).toComposeImageBitmap()
