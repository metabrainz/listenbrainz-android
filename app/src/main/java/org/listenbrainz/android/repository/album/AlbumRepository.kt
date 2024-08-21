package org.listenbrainz.android.repository.album


import org.listenbrainz.android.model.album.Album
import org.listenbrainz.android.model.album.AlbumInfo
import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.util.Resource

interface AlbumRepository {
    suspend fun fetchAlbumInfo(albumMbid: String?): Resource<AlbumInfo?>
    suspend fun fetchAlbum(albumMbid: String?): Resource<Album?>
    suspend fun fetchAlbumReviews(albumMbid: String?): Resource<CBReview?>
}
