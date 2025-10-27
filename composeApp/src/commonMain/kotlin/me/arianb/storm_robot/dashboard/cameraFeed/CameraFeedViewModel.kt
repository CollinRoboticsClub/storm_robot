package me.arianb.storm_robot.dashboard.cameraFeed

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.resilience.Schedule
import arrow.resilience.retry
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.arianb.storm_robot.MeasureCountPerTime
import me.arianb.storm_robot.ResilientService
import me.arianb.storm_robot.Server
import me.arianb.storm_robot.WebcamIdentifier
import me.arianb.storm_robot.applyCommonHttpClientConfig
import me.arianb.storm_robot.settings.UserPreferencesRepository
import kotlin.time.Duration.Companion.seconds

sealed class CameraFeedState {
    data object NotYetAttemptedConnection : CameraFeedState()
    data object CurrentlyAttemptingConnection : CameraFeedState()
    class CurrentlyConnected(val currentFrame: ImageBitmap) : CameraFeedState()
    data object StoppedConnection : CameraFeedState()
    class FailedToConnect(val error: Throwable) : CameraFeedState()
}

typealias WebcamIdInt = Int

// TODO: verify that refactoring to use a ViewModel didn't incur a noticeable performance penalty
class CameraFeedViewModel : ViewModel() {
    data class WebcamHolder(val service: ResilientService<*>, val state: StateFlow<CameraFeedState>)

    private val _availableWebcams = MutableStateFlow<List<WebcamIdentifier>>(emptyList())
    val availableWebcams: StateFlow<List<WebcamIdentifier>> = _availableWebcams

    val activeWebcamMap = mutableMapOf<WebcamIdInt, WebcamHolder>()

    // User preferences
    private val userPreferencesRepository = UserPreferencesRepository.getInstance()
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Camera feed job
    private val jobCoroutineScope = CoroutineScope(Dispatchers.IO)

    private val client = HttpClient {
        applyCommonHttpClientConfig()
    }

    init {
        // Scan for available cameras on startup
        rescanAvailableWebcams()
    }

    fun rescanAvailableWebcams() = viewModelScope.launch(Dispatchers.Default) {
        val response: HttpResponse = Schedule.exponential<Throwable>(1.seconds).retry {
            client.request(Server.Endpoints.VIDEO + "/info") {
                host = userPreferencesFlow.value.serverHost
                port = userPreferencesFlow.value.serverPort
            }
        }

        val availableWebcams: List<WebcamIdentifier> = response.body()

        _availableWebcams.update { availableWebcams }
    }

    fun start(webcamId: WebcamIdInt): StateFlow<CameraFeedState> {
        val webcamHolder: WebcamHolder = activeWebcamMap.getOrElse(webcamId) {
            val mutableCameraFeedState = MutableStateFlow<CameraFeedState>(CameraFeedState.NotYetAttemptedConnection)
            val cameraFeed = CameraFeed(webcamId)

            val resilientService = ResilientService(
                coroutineScope = jobCoroutineScope,
                flow = userPreferencesFlow,
                block = { userPreferences ->
                    mutableCameraFeedState.update { CameraFeedState.CurrentlyAttemptingConnection }
                    cameraFeed.start(
                        client = client,
                        host = userPreferences.serverHost,
                        port = userPreferences.serverPort,
                        onConnectionError = { t ->
                            mutableCameraFeedState.update { CameraFeedState.FailedToConnect(t) }
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
                for (frame in cameraFeed.frameChannel) {
                    mutableCameraFeedState.update { CameraFeedState.CurrentlyConnected(frame) }
                    profilingThing.check()
                }
            }

            val webcamHolder = WebcamHolder(resilientService, mutableCameraFeedState.asStateFlow())
            this.activeWebcamMap[webcamId] = webcamHolder
            webcamHolder
        }

        return webcamHolder.state
    }

    fun restart(webcamId: WebcamIdInt) {
        activeWebcamMap[webcamId]?.let {
            it.service.restart()
        }
    }

    fun stop(webcamId: WebcamIdInt) {
        activeWebcamMap[webcamId]?.let {
            it.service.stop()
//            it.state.update { CameraFeedState.StoppedConnection }
        }
    }
}
