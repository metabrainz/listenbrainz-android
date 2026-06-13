package org.listenbrainz.shared.repository.artist

import org.listenbrainz.shared.model.artist.ArtistPayload
import org.listenbrainz.shared.model.artist.ArtistWikiExtract
import org.listenbrainz.shared.model.artist.CBReview
import org.listenbrainz.shared.model.artistSearch.ArtistSearchPayload
import org.listenbrainz.shared.util.Resource

interface ArtistRepository {
    suspend fun fetchArtistData(artistMbid: String?): Resource<ArtistPayload?>
    suspend fun fetchArtistWikiExtract(artistMbid: String?): Resource<ArtistWikiExtract?>
    suspend fun fetchArtistReviews(artistMbid: String?): Resource<CBReview?>
    suspend fun searchArtist(query:String?): Resource<ArtistSearchPayload?>
}
