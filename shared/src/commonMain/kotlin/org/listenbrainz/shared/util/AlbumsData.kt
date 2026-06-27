package org.listenbrainz.shared.util

import org.listenbrainz.shared.model.Album

abstract class AlbumsData {

    /** Runtime cache to improve performance
     *  Not using on device caching as songs need to be refreshed frequently.*/
    private var albumsListCache = listOf<Album>()

    protected abstract fun albums(): List<Album>

    /** Fetch albums from device. Heavy task, so perform in `Dispacthers.IO`.*/
    fun fetchAlbums(userRequestedRefresh: Boolean = false): List<Album> {
        if(albumsListCache.isNotEmpty() && !userRequestedRefresh){
            return albumsListCache
        }
        albumsListCache = albums()
        return albumsListCache
    }

}