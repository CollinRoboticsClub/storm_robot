package me.arianb.storm_robot

data object SERVER {
    const val HOST = "192.168.203.1"
    const val PORT = 8080

    const val PING_PERIOD_MILLIS: Long = 15_000
    const val TIMEOUT_MILLIS: Long = 15_000

    data object ENDPOINTS {
        const val API_ROOT = "/api"
    }
}

data object CAMERA {
    const val RESOLUTION_WIDTH = 1280
    const val RESOLUTION_HEIGHT = 720
    const val EXPECTED_FPS = 30
    const val ASPECT_RATIO = 16f / 9f
}
