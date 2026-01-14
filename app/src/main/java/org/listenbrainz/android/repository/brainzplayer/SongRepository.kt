package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.shared.model.Song

interface SongRepository {
    fun getSongsStream() : Flow<List<Song>>
    fun getRecentlyPlayedSongs() : Flow<List<Song>>
    fun getSongsPlayedToday() : Flow<List<Song>>
    fun getSongsPlayedThisWeek() : Flow<List<Song>>
    suspend fun addSongs(userRequestedRefresh: Boolean = false): Boolean
    suspend fun updateSong(song : Song)

}