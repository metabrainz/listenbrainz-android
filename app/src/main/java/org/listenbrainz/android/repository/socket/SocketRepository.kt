package org.listenbrainz.android.repository.socket

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Listen

interface SocketRepository {

    fun listen(username: String): Flow<Listen>

}
