package me.arianb.storm_robot

import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.ensureActive

// Kinda lame code to centralize exception handling in websocket clients. Not sure how to improve it yet.
suspend fun HttpClient.websocketCatching(
    method: HttpMethod = HttpMethod.Get,
    host: String? = null,
    port: Int? = null,
    path: String? = null,
    request: HttpRequestBuilder.() -> Unit = {},
    onConnectionError: (Throwable) -> Unit = {},
    onErrorInBlock: (Throwable) -> Unit = {}, // TODO: improve name
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
                Logger.DEFAULT.log("CancellationException")

                // Propagate the cancellation
                ensureActive()

                Logger.DEFAULT.log("CancellationException: we didn't re-throw it")
            } catch (e: ClosedReceiveChannelException) {
                Logger.DEFAULT.log(
                    "server disconnected from websocket, reason: ${closeReason.await()}"
                )
            } catch (t: Throwable) {
                Logger.DEFAULT.log("onError ${closeReason.await()}")
                Logger.DEFAULT.log(t.stackTraceToString())

                onErrorInBlock(t)
            } finally {
                close()
            }
        }
    } catch (t: Throwable) {
        Logger.DEFAULT.log("Failed to connect to websocket with throwable: $t")
        onConnectionError(t)
    }
}