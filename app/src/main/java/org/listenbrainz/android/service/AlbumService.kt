package org.listenbrainz.android.service

import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import org.listenbrainz.android.model.album.Album
interface AlbumService {
    @POST("album/{album_mbid}/")
    suspend fun getAlbumData(@Path("album_mbid") albumMbid: String): Album
}