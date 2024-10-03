package me.arianb.storm_robot.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.arianb.storm_robot.SERVER

class SettingsViewModel : ViewModel() {
    val userPreferencesFlow = MutableStateFlow(UserPreferences())
}

data class UserPreferences(
    val serverIP: String = SERVER.HOST,
    val serverPort: Int = SERVER.PORT,
)