package me.arianb.storm_robot

data object Server {
    const val HOST = "localhost"
    const val PORT = 8080

    const val PING_PERIOD_MILLIS: Long = 15_000
    const val TIMEOUT_MILLIS: Long = 15_000

    data object Endpoints {
        const val API_ROOT = "/api"
        const val API_WHEELS = "/wheels"
        const val API_ARM = "/arm"
        const val API_IR_MESSAGES = "/messages"

        const val VIDEO = "/video"
    }
}

data object CAMERA {
    const val RESOLUTION_WIDTH = 1280
    const val RESOLUTION_HEIGHT = 720
    const val EXPECTED_FPS = 30
    const val ASPECT_RATIO = 16f / 9f
}
