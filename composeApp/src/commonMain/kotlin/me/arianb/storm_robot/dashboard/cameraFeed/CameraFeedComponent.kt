package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.arianb.storm_robot.CAMERA
import me.arianb.storm_robot.LabeledIconImage
import org.jetbrains.compose.resources.painterResource
import storm_robot.composeapp.generated.resources.MoreHoriz
import storm_robot.composeapp.generated.resources.Res
import storm_robot.composeapp.generated.resources.error

@Composable
fun CameraWindow(cameraFeedViewModel: CameraFeedViewModel = viewModel()) {
    Surface(
        modifier = Modifier.aspectRatio(CAMERA.ASPECT_RATIO),
        color = MaterialTheme.colorScheme.primary
    ) {
        val cameraFeedState by cameraFeedViewModel.cameraFeedState.collectAsState()

        // OPTIMIZEME:
        //  if I want to improve rendering performance later on, I can reference this: https://github.com/JetBrains/skiko
        //  sample code to maybe draw in a more "raw"/"immediate" way by bypassing Compose.
        //
        // NOTE:
        //  assigning the cameraFeedState to a separate var to allow me to smart cast it, because
        //  otherwise it'll complain that the variable could've been changed by the time it gets
        //  to the FailedToConnect code.
        when (val thisCameraFeedState = cameraFeedState) {
            is CameraFeedState.CurrentlyConnected -> {
                Image(thisCameraFeedState.currentFrame, null)
            }

            CameraFeedState.NotYetAttemptedConnection -> {
                LabeledIconImage(
                    iconVector = Icons.Default.Check,
                    label = "Haven't attempted to connect to camera feed yet.",
                )
            }

            CameraFeedState.CurrentlyAttemptingConnection -> {
                LabeledIconImage(
                    iconPainter = painterResource(Res.drawable.MoreHoriz),
                    label = "Attempting to connect to stream...",
                )
            }

            CameraFeedState.StoppedConnection -> {
                LabeledIconImage(
                    iconVector = Icons.Default.Close,
                    label = "Camera stream was stopped",
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
    HorizontalDivider()
    CameraFeedControls()
}

@Composable
fun CameraFeedControls(cameraFeedViewModel: CameraFeedViewModel = viewModel()) {
    Surface(
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { cameraFeedViewModel.restart() }) {
                Icon(Icons.Default.Refresh, null)
            }
            IconButton(onClick = { cameraFeedViewModel.stop() }) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}
