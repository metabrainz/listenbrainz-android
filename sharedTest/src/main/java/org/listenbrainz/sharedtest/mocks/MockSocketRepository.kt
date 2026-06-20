package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.shared.model.Listen
import org.listenbrainz.shared.repository.socket.SocketRepository

class MockSocketRepository : SocketRepository {
    override fun listen(usernameProvider: suspend () -> String): Flow<Listen?> {
        return flow {  }
    }
}