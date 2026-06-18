package org.listenbrainz.shared.repository.socket

import com.piasy.kmp.socketio.socketio.IO
import com.piasy.kmp.socketio.socketio.Socket
import io.ktor.client.HttpClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.listenbrainz.shared.model.Listen
import org.listenbrainz.shared.util.Log


class SocketRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val logger:Log = Log
) : SocketRepository {

    override fun listen(usernameProvider: suspend () -> String) = callbackFlow {
        val username = usernameProvider()
        val options = IO.Options().apply {
            httpClient = this@SocketRepositoryImpl.httpClient
            transports = listOf("websocket")
        }

        var activeSocket: Socket? = null

        IO.socket("https://listenbrainz.org", options) { socket ->
            activeSocket = socket
            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("json", buildJsonObject { put("user", username) })
            }
            listOf("playing_now", "listen").forEach { event ->
                socket.on(event) { data ->
                    try {
                        val payload = data.firstOrNull()?.toString() ?: return@on
                        val listen = json.decodeFromString<Listen>(payload)
                        trySendBlocking(listen).onFailure {
                            it?.printStackTrace()
                        }
                    } catch (e: Exception) {
                        logger.e("SocketRepository: $event error ${e.message}")
                    }
                }
            }
            socket.open()
        }

        awaitClose {
            activeSocket?.close()
        }
    }
}
