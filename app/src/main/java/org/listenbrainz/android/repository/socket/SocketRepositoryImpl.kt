package org.listenbrainz.android.repository.socket

import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_CONNECT
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.util.Log
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(): SocketRepository {
    
    private val socket: Socket = IO.socket(
        "https://listenbrainz.org/",
        IO.Options.builder().setPath("/socket.io/").build()
    )

    override fun listen(username: String) = callbackFlow {
        socket
            .on(EVENT_CONNECT) {
                Log.d("Listen socket connected.")
                socket.emit("json", JSONObject().put("user", username))
            }
            .on("playing_now") {
                val listen = Gson().fromJson(it[0] as String, Listen::class.java)
                trySendBlocking(listen)
                    .onFailure { throwable ->
                        throwable?.printStackTrace()
                    }
            }
            .on("listen") {
                val listen = Gson().fromJson(it[0] as String, Listen::class.java)
                trySendBlocking(listen)
                    .onFailure { throwable ->
                        throwable?.printStackTrace()
                    }
            }
        
        socket.connect()

        awaitClose {
            socket.disconnect()
            socket.off()
        }
    }

}
