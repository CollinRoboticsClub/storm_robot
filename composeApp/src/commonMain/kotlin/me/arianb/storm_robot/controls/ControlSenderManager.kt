package me.arianb.storm_robot.controls

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import me.arianb.storm_robot.ResilientService
import me.arianb.storm_robot.settings.UserPreferencesRepository

sealed class ControlSenderState {
    data object NotYetAttemptedConnection : ControlSenderState()
    data object CurrentlyAttemptingConnection : ControlSenderState()
    class CurrentlyConnected() : ControlSenderState()
    data object StoppedConnection : ControlSenderState()
    class FailedToConnect(val error: Throwable) : ControlSenderState()
}

object ControlSenderManager {
    private var _controlSenderState = MutableStateFlow<ControlSenderState>(ControlSenderState.NotYetAttemptedConnection)
    val controlSenderState: StateFlow<ControlSenderState> = _controlSenderState

    // User preferences
    private val userPreferencesRepository = UserPreferencesRepository.getInstance()
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Control sender job
    private val jobCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val resilientService = ResilientService(
        coroutineScope = jobCoroutineScope,
        flow = userPreferencesFlow,
        block = { userPreferences ->
            _controlSenderState.update { ControlSenderState.CurrentlyAttemptingConnection }
            ControlSender.start(
                host = userPreferences.serverHost,
                port = userPreferences.serverPort,
                onConnectionError = { t ->
                    _controlSenderState.update { ControlSenderState.FailedToConnect(t) }

                    // re-throw to make this retry
                    throw t
                },
                onErrorInBlock = { t ->
                    Logger.w("ControlSender.start() has exited (but should retry itself soon) with throwable: $t")

                    // re-throw to make this retry
                    throw t
                }
            )

            Logger.e("CRITICAL BUG: ControlSender died. This message should never run since this service should run forever")
        }
    )
}
