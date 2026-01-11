package org.listenbrainz.android.repository.artist

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.artist.ArtistPayload
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.service.ArtistService
import org.listenbrainz.android.service.CBService
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class ArtistRepositoryImpl @Inject constructor(
    private val service: ArtistService,
    private val mbService: MBService,
    private val cbService: CBService,
) : ArtistRepository {
    override suspend fun fetchArtistData(artistMbid: String?): Resource<ArtistPayload?> = parseResponse {
        failIf(artistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getArtistData(artistMbid)
    }

    override suspend fun fetchArtistWikiExtract(artistMbid: String?): Resource<ArtistWikiExtract?> = parseResponse {
        failIf(artistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        mbService.getArtistWikiExtract(artistMbid)
    }

    override suspend fun fetchArtistReviews(artistMbid: String?): Resource<CBReview?> = parseResponse {
        failIf(artistMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        cbService.getArtistReviews(artistMbid)
    }
}
