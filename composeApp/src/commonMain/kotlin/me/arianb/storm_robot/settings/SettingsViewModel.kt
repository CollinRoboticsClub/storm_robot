package me.arianb.storm_robot.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.arianb.storm_robot.SERVER
import me.arianb.storm_robot.dashboard.cameraFeed.CameraFeed

class SettingsViewModel : ViewModel() {
    private val _userPreferencesFlow = MutableStateFlow(UserPreferences())
    val userPreferences = _userPreferencesFlow.asStateFlow()

    fun setServerHost(host: String) {
        CameraFeed.serverHost = host
        _userPreferencesFlow.update {
            userPreferences.value.copy(serverHost = host)
        }
    }

    fun setServerPort(port: Int) {
        CameraFeed.serverPort = port
        _userPreferencesFlow.update {
            userPreferences.value.copy(serverPort = port)
        }
    }
}

data class UserPreferences(
    val serverHost: String = SERVER.HOST,
    val serverPort: Int = SERVER.PORT,
)