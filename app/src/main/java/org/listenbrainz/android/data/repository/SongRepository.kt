package org.listenbrainz.android.data.repository

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.Song

interface SongRepository {
    fun getSongsStream() : Flow<List<Song>>
    suspend fun addSongs(userRequestedRefresh: Boolean = false): Boolean

}