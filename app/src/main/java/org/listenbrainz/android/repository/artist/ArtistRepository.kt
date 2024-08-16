package org.listenbrainz.android.repository.artist


import org.listenbrainz.android.model.artist.ArtistBio
import org.listenbrainz.android.util.Resource

interface ArtistRepository {
    suspend fun fetchArtistBio(artistMbid: String?) : Resource<ArtistBio?>
}
