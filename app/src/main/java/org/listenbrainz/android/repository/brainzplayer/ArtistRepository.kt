package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song

interface ArtistRepository {
 fun getArtist(artistID: String) : Flow<Artist>
 fun getArtists(): Flow<List<Artist>>
 suspend fun addArtists(userRequestedRefresh: Boolean = false): Boolean
 suspend fun addAllSongsOfArtist(artist: Artist, userRequestedRefresh: Boolean): List<Song>
 suspend fun addAllAlbumsOfArtist(artist: Artist): List<Album>
}