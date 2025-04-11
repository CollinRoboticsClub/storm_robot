package me.arianb.storm_robot

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.ensureActive

fun HttpClientConfig<*>.applyCommonHttpClientConfig() {
    install(WebSockets) {
        pingIntervalMillis = Server.PING_PERIOD_MILLIS
        maxFrameSize = Server.WEBSOCKET_MAX_FRAME_SIZE
    }
    install(Logging)
}

// Kinda lame code to centralize exception handling in websocket clients. Not sure how to improve it yet.
suspend fun HttpClient.websocketCatching(
    method: HttpMethod = HttpMethod.Get,
    host: String? = null,
    port: Int? = null,
    path: String? = null,
    request: HttpRequestBuilder.() -> Unit = {},
    onConnectionError: (Throwable) -> Unit = {},
    onErrorInBlock: (Throwable) -> Unit = {},
    block: suspend DefaultClientWebSocketSession.() -> Unit
) {
    try {
        webSocket(
            method = method,
            host = host,
            port = port,
            path = path,
            request = request,
        ) {
            try {
                block()
            } catch (e: CancellationException) {
                Logger.d("CancellationException")

                // Propagate the cancellation
                ensureActive()

                Logger.w("CancellationException: we didn't re-throw it")
            } catch (e: ClosedReceiveChannelException) {
                Logger.i(
                    "server disconnected from websocket, reason: ${closeReason.await()}"
                )
            } catch (t: Throwable) {
                Logger.w("onError ${closeReason.await()}")
                Logger.w(t.stackTraceToString())

                onErrorInBlock(t)
            } finally {
                close()
            }
        }
    } catch (t: Throwable) {
        Logger.w("Failed to connect to websocket with throwable: $t")
        onConnectionError(t)
    }
}
