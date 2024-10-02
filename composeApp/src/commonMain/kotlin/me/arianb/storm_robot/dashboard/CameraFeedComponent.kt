package me.arianb.storm_robot.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.arianb.storm_robot.CAMERA
import me.arianb.storm_robot.CameraFeed
import me.arianb.storm_robot.LabeledIconImage
import org.jetbrains.compose.resources.painterResource
import storm_robot.composeapp.generated.resources.Res
import storm_robot.composeapp.generated.resources.error

sealed class CameraFeedState {
    data object NotYetAttemptedConnection : CameraFeedState()
    data object CurrentlyConnected : CameraFeedState()
    class FailedToConnect(val error: Throwable) : CameraFeedState()
}

// NOTE:
//  Apparently mutating `remember`d states needs to happen on the Main thread to avoid exceptions due
//  to snapshot mutability policies or something along those lines. Seems like low-level Compose stuff.
//  This method basically just exists as a little shortcut for this + a place to keep this note.
suspend fun <T> runOnMain(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Main, block)

@Composable
fun CameraWindow() {
    val thisCoroutineScope = rememberCoroutineScope()

    var cameraFeedState: CameraFeedState by remember { mutableStateOf(CameraFeedState.NotYetAttemptedConnection) }

    // NOTE:
    //  the width and height of this must be > 0 even if it's just a blank placeholder, because
    //  if it is ever drawn and has a size of 0, then some low level drawing code crashes when trying
    //  to draw something of size NaN.
    val blankImageBitmap: ImageBitmap = remember { ImageBitmap(1, 1) }

    // Default is a blank image bitmap of size 0x0
    var currentCameraFeedFrame: ImageBitmap by remember { mutableStateOf(blankImageBitmap) }

    thisCoroutineScope.launch(Dispatchers.IO) {
        try {
            coroutineScope {
                launch {
                    CameraFeed.start()
                }
                launch {
//                    val profilingThing = MeasureCountPerTime(1.seconds)
                    runOnMain { cameraFeedState = CameraFeedState.CurrentlyConnected }
                    for (frame in CameraFeed.frameChannel) {
                        currentCameraFeedFrame = frame

//                        profilingThing.check()
                    }
                }
            }
        } catch (t: Throwable) {
            runOnMain { cameraFeedState = CameraFeedState.FailedToConnect(t) }
        }
    }

    Surface(
        modifier = Modifier.aspectRatio(CAMERA.ASPECT_RATIO),
        color = MaterialTheme.colorScheme.primary
    ) {
        // TODO:
        //  if I want to improve rendering performance later on, I can reference this: https://github.com/JetBrains/skiko
        //  sample code to maybe draw in a more "raw"/"immediate" way by bypassing Compose.
        //
        // TODO: verify that the clean error-handling isn't adding overhead to the rendering.
        //
        // NOTE:
        //  assigning the cameraFeedState to a separate var to allow me to smart cast it, because
        //  otherwise it'll complain that the variable could've been changed by the time it gets
        //  to the FailedToConnect code.
        when (val thisCameraFeedState = cameraFeedState) {
            CameraFeedState.CurrentlyConnected -> {
                Image(currentCameraFeedFrame, null)
            }

            CameraFeedState.NotYetAttemptedConnection -> {
                LabeledIconImage(
                    iconVector = Icons.Default.ThumbUp,
                    label = "Haven't attempted to connect to camera feed yet.",
                )
            }

            is CameraFeedState.FailedToConnect -> {
                LabeledIconImage(
                    iconPainter = painterResource(Res.drawable.error),
                    label = "Error: ${thisCameraFeedState.error.message}",
                )
            }
        }
    }
}