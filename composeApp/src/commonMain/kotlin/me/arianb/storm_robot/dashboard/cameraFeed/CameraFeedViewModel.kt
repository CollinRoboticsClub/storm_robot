package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // Camera feed job
    private val cameraCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val cameraFeedJob = RestartableJob(
        coroutineScope = cameraCoroutineScope,
        block = {
            CameraFeed.start()
        },
        handler = CoroutineExceptionHandler { _, t ->
            _cameraFeedState.update { CameraFeedState.FailedToConnect(t) }
        }
    )

    init {
        start()

        // The frame update loop should only be run once, because it should never die.
        // If I'm wrong about that, some more logic will need to be added
        cameraCoroutineScope.launch {
            //val profilingThing = MeasureCountPerTime(1.seconds)
            for (frame in CameraFeed.frameChannel) {
                _cameraFeedState.update { CameraFeedState.CurrentlyConnected(frame) }
                //profilingThing.check()
            }
        }
    }

    private fun start() {
        _cameraFeedState.update { CameraFeedState.CurrentlyAttemptingConnection }
        cameraFeedJob.start()
    }

    fun restart() {
        cameraFeedJob.stop()
        start()
    }

    fun stop() {
        cameraFeedJob.stop()
        _cameraFeedState.update { CameraFeedState.StoppedConnection }
    }
}
