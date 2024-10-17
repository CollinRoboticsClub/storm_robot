package me.arianb.storm_robot

// typealias-ing this in case I want to change all their types in the future
typealias EndpointPath = String

data object Server {
    const val HOST: String = "localhost"
    const val PORT: Int = 8080

    const val PING_PERIOD_MILLIS: Long = 15_000
    const val TIMEOUT_MILLIS: Long = 15_000
    const val WEBSOCKET_MAX_FRAME_SIZE: Long = Long.MAX_VALUE

    data object Endpoints {
        const val API_ROOT: EndpointPath = "/api"

        // TODO: add endpoint sub-paths
        const val API_WHEELS: EndpointPath = "/wheels"
        const val API_ARM: EndpointPath = "/arm"
        const val API_IR_MESSAGES: EndpointPath = "/messages"

        const val VIDEO: EndpointPath = "/video"
    }
}

typealias ResolutionSize = Int

data object CAMERA {
    const val RESOLUTION_WIDTH: ResolutionSize = 1280
    const val RESOLUTION_HEIGHT: ResolutionSize = 720
    const val EXPECTED_FPS: Int = 30
    const val ASPECT_RATIO: Float = 16f / 9f
}
