package org.listenbrainz.shared.repository.artist

import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.artist.ArtistPayload
import org.listenbrainz.shared.model.artist.ArtistWikiExtract
import org.listenbrainz.shared.model.artist.CBReview
import org.listenbrainz.shared.model.artistSearch.ArtistSearchPayload
import org.listenbrainz.shared.service.ArtistService
import org.listenbrainz.shared.service.CBService
import org.listenbrainz.shared.service.MBService
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Utils.parseResponse


class ArtistRepositoryImpl(
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

    override suspend fun searchArtist(query: String?): Resource<ArtistSearchPayload?> = parseResponse {
        failIf(query.isNullOrEmpty()) { ResponseError.BadRequest() }
        mbService.searchArtist(query)
    }
}
