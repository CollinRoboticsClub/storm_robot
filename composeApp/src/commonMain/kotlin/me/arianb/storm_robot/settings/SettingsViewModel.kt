package me.arianb.storm_robot.settings

import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val userPreferencesRepository = UserPreferencesRepository.getInstance()
    val userPreferences = userPreferencesRepository.userPreferencesFlow

    // Pass through these functions
    val setServerHost = userPreferencesRepository::setServerHost
    val setServerPort = userPreferencesRepository::setServerPort
}
