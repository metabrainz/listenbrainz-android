package org.listenbrainz.shared.repository.socket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.listenbrainz.shared.model.Listen


class SocketRepositoryImpl(
    val httpClient: HttpClient
) : SocketRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun listen(usernameProvider: suspend () -> String) = callbackFlow {
        val username = usernameProvider()
        httpClient.webSocket(
            urlString = "wss://listenbrainz.org/socket.io/?EIO=4&transport=websocket"
        ) {
            send(Frame.Text("40"))
            val subscribeToUser = """42["json",{"user": "${username}"}]"""
            send(Frame.Text(subscribeToUser))

            for (frame in incoming) {
                if (frame !is Frame.Text) {
                    continue
                }

                val data = frame.readText()

                when {
                    data == "2" -> send(Frame.Text("3"))

                    data.startsWith("42") -> {
                        try {
                            val jsonData = data.removePrefix("42")
                            val array = json.parseToJsonElement(jsonData) as JsonArray
                            val eventType = array[0].jsonPrimitive.content
                            val payload = array[1].jsonPrimitive.content
                            when (eventType) {
                                "playing_now", "listen" -> {
                                    val listen = json.decodeFromString<Listen>(payload)
                                    trySendBlocking(listen)
                                        .onFailure {
                                            it?.printStackTrace()
                                        }
                                }
                            }
                        } catch (e: Exception) {
                            print("SocketRepository: error ${e.message}")
                        }
                    }
                }
            }
        }
    }
}
