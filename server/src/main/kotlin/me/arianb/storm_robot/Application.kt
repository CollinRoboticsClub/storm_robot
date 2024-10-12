package me.arianb.storm_robot

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import me.arianb.storm_robot.arm.armRoutes

fun main() {
    embeddedServer(Netty, port = Server.PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets) {
        pingPeriodMillis = Server.PING_PERIOD_MILLIS
        timeoutMillis = Server.TIMEOUT_MILLIS
        maxFrameSize = Long.MAX_VALUE
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        masking = false
    }

    routing {
        route(Server.Endpoints.API_ROOT) {
            route(Server.Endpoints.API_WHEELS) {
                wheelsRoutes()
            }
            route(Server.Endpoints.API_ARM) {
                armRoutes()
            }
            route(Server.Endpoints.API_IR_MESSAGES) {
                IRMessagesRoutes()
            }
        }

        route(Server.Endpoints.VIDEO) {
            cameraRoutes()
        }
    }
}
