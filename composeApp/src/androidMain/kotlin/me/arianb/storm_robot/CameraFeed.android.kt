package me.arianb.storm_robot

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun bytesToImageBitmap(frameBytes: ByteArray): ImageBitmap =
    BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.size).asImageBitmap()
