package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.repository.socket.SocketRepository

class MockSocketRepository : SocketRepository {
    override fun listen(username: String): Flow<Listen> {
        return flow {  }
    }

}