package me.arianb.storm_robot.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import me.arianb.storm_robot.Server

data class UserPreferences(
    val serverHost: String,
    val serverPort: Int,
)

class UserPreferencesRepository private constructor() {
    private val _userPreferencesFlow = MutableStateFlow(initialUserPreferences)
    val userPreferencesFlow: StateFlow<UserPreferences> = _userPreferencesFlow

    private val initialUserPreferences: UserPreferences
        get() {
            // If we want to make settings persistent later, this should grab the user's saved preferences,
            // rather than just using defaults
            return UserPreferences(
                serverHost = Server.HOST,
                serverPort = Server.PORT,
            )
        }

    fun setServerHost(host: String) {
        _userPreferencesFlow.update {
            it.copy(serverHost = host)
        }
    }

    fun setServerPort(port: Int) {
        _userPreferencesFlow.update {
            it.copy(serverPort = port)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = UserPreferencesRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
