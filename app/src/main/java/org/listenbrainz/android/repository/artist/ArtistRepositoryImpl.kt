package org.listenbrainz.android.repository.artist

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.artist.ArtistBio
import org.listenbrainz.android.service.ArtistService
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class ArtistRepositoryImpl @Inject constructor(
    private val service: ArtistService,
    private val mbService: MBService,
) : ArtistRepository {
    override suspend fun fetchArtistBio(artistMbid: String?): Resource<ArtistBio?> = parseResponse {
        if (artistMbid.isNullOrEmpty()) return ResponseError.BAD_REQUEST.asResource()
        mbService.getArtistBio(artistMbid)
    }
}
