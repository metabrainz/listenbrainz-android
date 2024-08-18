package org.listenbrainz.android.repository.artist


import org.listenbrainz.android.model.artist.ArtistPayload
import org.listenbrainz.android.model.artist.ArtistReview
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.util.Resource

interface ArtistRepository {
    suspend fun fetchArtistData(artistMbid: String?): Resource<ArtistPayload?>
    suspend fun fetchArtistWikiExtract(artistMbid: String?): Resource<ArtistWikiExtract?>
    suspend fun fetchArtistReviews(artistMbid: String?): Resource<ArtistReview?>
}
