package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.Song

abstract class SongsData {
    // Temporary cache
    private var songsListCache = listOf<Song>()

    protected abstract fun songs(): List<Song>

    /** Update cached songs list on demand. */
    fun updateCache(){
        fetchSongs(userRequestedRefresh = true)
    }

    /** Fetch songs from device. Heavy task, so perform in `Dispacthers.IO`.*/
    fun fetchSongs(userRequestedRefresh: Boolean = false): List<Song> {
        if (songsListCache.isNotEmpty() && !userRequestedRefresh){
            return songsListCache
        }
        songsListCache = songs()
        return songsListCache
    }
}