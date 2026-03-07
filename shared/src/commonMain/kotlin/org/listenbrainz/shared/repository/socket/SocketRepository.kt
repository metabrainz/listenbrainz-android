package org.listenbrainz.shared.repository.socket

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.shared.model.Listen

interface SocketRepository {

    fun listen(usernameProvider: suspend () -> String): Flow<Listen?>

}