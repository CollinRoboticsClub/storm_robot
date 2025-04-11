package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.arianb.storm_robot.MeasureCountPerTime
import me.arianb.storm_robot.ResilientService
import me.arianb.storm_robot.settings.UserPreferencesRepository
import kotlin.time.Duration.Companion.seconds

sealed class CameraFeedState {
    data object NotYetAttemptedConnection : CameraFeedState()
    data object CurrentlyAttemptingConnection : CameraFeedState()
    class CurrentlyConnected(val currentFrame: ImageBitmap) : CameraFeedState()
    data object StoppedConnection : CameraFeedState()
    class FailedToConnect(val error: Throwable) : CameraFeedState()
}

// TODO: verify that refactoring to use a ViewModel didn't incur a noticeable performance penalty
class CameraFeedViewModel : ViewModel() {
    // Camera feed state
    private var _cameraFeedState = MutableStateFlow<CameraFeedState>(CameraFeedState.NotYetAttemptedConnection)
    val cameraFeedState: StateFlow<CameraFeedState> = _cameraFeedState

    // User preferences
    private val userPreferencesRepository = UserPreferencesRepository.getInstance()
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Camera feed job
    private val jobCoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        val resilientService = ResilientService(
            coroutineScope = jobCoroutineScope,
            flow = userPreferencesFlow,
            block = { userPreferences ->
                _cameraFeedState.update { CameraFeedState.CurrentlyAttemptingConnection }
                CameraFeed.start(
                    host = userPreferences.serverHost,
                    port = userPreferences.serverPort,
                    onConnectionError = { t ->
                        _cameraFeedState.update { CameraFeedState.FailedToConnect(t) }
                    },
                )

                // Wait a bit before restarting
                delay(1000)
            }
        )

        // The frame update loop should only be run once, because it should never die.
        // If I'm wrong about that, some more logic will need to be added
        jobCoroutineScope.launch {
            val profilingThing = MeasureCountPerTime(1.seconds)
            for (frame in CameraFeed.frameChannel) {
                _cameraFeedState.update { CameraFeedState.CurrentlyConnected(frame) }
                profilingThing.check()
            }
        }
    }

    fun restart() {
//        job.stop()
//        startOld()
    }

    fun stop() {
//        job.stop()
//        _cameraFeedState.update { CameraFeedState.StoppedConnection }
    }
}
