package org.listenbrainz.android.repository.album

import org.listenbrainz.shared.model.album.AlbumInfo
import org.listenbrainz.shared.model.albumSearch.AlbumSearchPayload
import org.listenbrainz.shared.model.album.Album
import org.listenbrainz.shared.model.artist.CBReview
import org.listenbrainz.shared.util.Resource

interface AlbumRepository {
    suspend fun fetchAlbumInfo(albumMbid: String?): Resource<AlbumInfo?>
    suspend fun fetchAlbum(albumMbid: String?): Resource<Album?>
    suspend fun fetchAlbumReviews(albumMbid: String?): Resource<CBReview?>
    suspend fun searchAlbums(query:String?) : Resource<AlbumSearchPayload?>
}
