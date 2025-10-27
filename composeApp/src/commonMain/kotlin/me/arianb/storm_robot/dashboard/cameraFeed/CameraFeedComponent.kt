package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import me.arianb.storm_robot.Camera
import me.arianb.storm_robot.LabeledIconImage
import me.arianb.storm_robot.WebcamIdentifier
import me.arianb.storm_robot.settings.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import storm_robot.composeapp.generated.resources.MoreHoriz
import storm_robot.composeapp.generated.resources.Res
import storm_robot.composeapp.generated.resources.error

@Composable
fun CameraWindow(
    cameraFeedViewModel: CameraFeedViewModel = viewModel(),
) {
    val availableWebcams: List<WebcamIdentifier> by cameraFeedViewModel.availableWebcams.collectAsState()

    var selectedWebcam by remember { mutableStateOf<WebcamIdentifier?>(null) }
    CameraDropdownMenu(availableWebcams) {
        selectedWebcam = it
    }

    CameraStreamWindow(selectedWebcam?.id)

    HorizontalDivider()
    CameraFeedControls(selectedWebcam?.id)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CameraDropdownMenu(
    availableWebcams: List<WebcamIdentifier>,
    onWebcamSelected: (WebcamIdentifier) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedWebcamString by remember { mutableStateOf<String?>(null) }

    fun WebcamIdentifier.toPrettyString(): String = "ID #${this.id}: ${this.name}"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (availableWebcams.isNotEmpty()) {
                expanded = true
            }
        },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            value = selectedWebcamString ?: if (availableWebcams.isEmpty()) {
                "Loading..."
            } else {
                "Select A Camera"
            },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Camera List") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableWebcams.forEach { webcam ->
                val itemString = webcam.toPrettyString()
                DropdownMenuItem(
                    text = {
                        Text(itemString)
                    },
                    onClick = {
                        expanded = false
                        selectedWebcamString = itemString

                        onWebcamSelected(webcam)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraStreamWindow(
    cameraId: WebcamIdInt?,
    cameraFeedViewModel: CameraFeedViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    Surface(
        modifier = Modifier.aspectRatio(Camera.ASPECT_RATIO),
        color = MaterialTheme.colorScheme.primary
    ) {
        if (cameraId == null) {
            LabeledIconImage(
                iconVector = Icons.Default.Close,
                label = "Select a camera stream",
            )
        } else {
            val cameraFeedState by cameraFeedViewModel.start(cameraId).collectAsState()
            val userPreferencesState by settingsViewModel.userPreferences.collectAsState()

            // OPTIMIZEME:
            //  if I want to improve rendering performance later on, I can reference this: https://github.com/JetBrains/skiko
            //  sample code to maybe draw in a more "raw"/"immediate" way by bypassing Compose.
            //
            // NOTE: assigning the cameraFeedState to a separate var to allow me to smart cast it, because it's a delegated property.
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
    }
}

@Composable
fun CameraFeedControls(
    cameraId: WebcamIdInt?,
    cameraFeedViewModel: CameraFeedViewModel = viewModel()
) {
    Surface(
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = {
//                cameraFeedViewModel.rescanAvailableWebcams()
                if (cameraId != null) {
                    cameraFeedViewModel.restart(cameraId)
                }
            }) {
                Icon(Icons.Default.Refresh, null)
            }
            IconButton(onClick = {
                if (cameraId != null) {
                    cameraFeedViewModel.restart(cameraId)
                }
            }) {
                Icon(Icons.Default.Close, null)
            }
        }
    }
}
