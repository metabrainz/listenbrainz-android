package org.listenbrainz.android.repository.album

import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.album.AlbumInfo
import org.listenbrainz.android.model.album.Album
import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.service.AlbumService
import org.listenbrainz.android.service.CBService
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.parseResponse
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val service: AlbumService,
    private val cbService: CBService,
    private val mbService: MBService
) : AlbumRepository {
    override suspend fun fetchAlbumInfo(albumMbid: String?): Resource<AlbumInfo?> = parseResponse {
        failIf(albumMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        mbService.getAlbumInfo(albumMbid)
    }

    override suspend fun fetchAlbum(albumMbid: String?): Resource<Album?> = parseResponse {
        failIf(albumMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        service.getAlbumData(albumMbid)
    }
    
    override suspend fun fetchAlbumReviews(albumMbid: String?): Resource<CBReview?> = parseResponse {
        failIf(albumMbid.isNullOrEmpty()) { ResponseError.BadRequest() }
        cbService.getArtistReviews(albumMbid, entityType = "release_group")
    }
}