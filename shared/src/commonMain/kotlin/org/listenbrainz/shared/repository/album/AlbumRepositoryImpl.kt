package org.listenbrainz.shared.repository.album

import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.album.AlbumInfo
import org.listenbrainz.shared.model.album.Album
import org.listenbrainz.shared.model.albumSearch.AlbumSearchPayload
import org.listenbrainz.shared.model.artist.CBReview
import org.listenbrainz.shared.service.AlbumService
import org.listenbrainz.shared.service.CBService
import org.listenbrainz.shared.service.MBService
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.Utils.parseResponse


class AlbumRepositoryImpl(
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

    override suspend fun searchAlbums(query: String?): Resource<AlbumSearchPayload?> = parseResponse{
        failIf(query.isNullOrEmpty()) { ResponseError.BadRequest() }
        mbService.searchAlbum(query)
    }
}