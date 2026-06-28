package org.listenbrainz.shared.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.shared.model.Album
import org.listenbrainz.shared.model.Artist
import org.listenbrainz.shared.model.Song

interface BPArtistRepository {
 fun getArtist(artistID: String) : Flow<Artist>
 fun getArtists(): Flow<List<Artist>>
 suspend fun addArtists(userRequestedRefresh: Boolean = false): Boolean
 suspend fun addAllSongsOfArtist(artist: Artist, userRequestedRefresh: Boolean): List<Song>
 suspend fun addAllAlbumsOfArtist(artist: Artist): List<Album>
}