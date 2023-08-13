package org.listenbrainz.android.repository.remoteplayer

import org.listenbrainz.android.util.Resource

interface RemotePlayerRepository {
    
    suspend fun searchYoutubeMusicVideoId(
        trackName: String,
        artist: String
    ): Resource<String>
    
    suspend fun playOnYoutube(
        getYoutubeMusicVideoId: suspend () -> Resource<String>
    ): Resource<Unit>
}