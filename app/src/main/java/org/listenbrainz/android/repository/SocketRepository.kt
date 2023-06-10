package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Listen

interface SocketRepository {

    fun listen(username: String): Flow<Listen>

}
