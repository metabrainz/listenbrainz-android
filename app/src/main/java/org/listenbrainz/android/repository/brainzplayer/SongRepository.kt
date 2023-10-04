package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Song

interface SongRepository {
    fun getSongsStream() : Flow<List<Song>>
    suspend fun addSongs(userRequestedRefresh: Boolean = false): Boolean

}