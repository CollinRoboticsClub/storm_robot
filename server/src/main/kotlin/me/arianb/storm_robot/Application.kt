package me.arianb.storm_robot

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import me.arianb.storm_robot.arm.armRoutes

fun main() {
    embeddedServer(Netty, port = SERVER.PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets) {
        pingPeriodMillis = SERVER.PING_PERIOD_MILLIS
        timeoutMillis = SERVER.TIMEOUT_MILLIS
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        // LEGACY: remove this once the client is more capable
        staticResources("/", "webui")

        // TODO: move paths to constants file?
        route(SERVER.ENDPOINTS.API_ROOT) {
            route("/wheels") {
                wheelsRoutes()
            }
            route("/arm") {
                armRoutes()
            }
            route("/messages") {
//                IRMessagesRoutes()
            }
        }

        route("/video") {
            cameraRoutes()
        }
    }
}
