package org.listenbrainz.android.util

import android.os.Build
import android.provider.MediaStore
import org.listenbrainz.android.application.App.Companion.context
import org.listenbrainz.android.model.Album
import javax.inject.Singleton

@Singleton
object AlbumsData {
    
    /** Runtime cache to improve performance
     *  Not using on device caching as songs need to be refreshed frequently.*/
    private var albumsListCache = listOf<Album>()
    
    /** Fetch albums from device. Heavy task, so perform in `Dispacthers.IO`.*/
    fun fetchAlbums(userRequestedRefresh: Boolean = false): List<Album> {
        if(albumsListCache.isNotEmpty() && !userRequestedRefresh){
            return albumsListCache
        }
        val albums = mutableListOf<Album>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val albumsProjections = arrayOf(
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST
        )
        val sortOrderAlbum = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC"
        val isMusic = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val albumQuery = context.contentResolver?.query(
            collection,
            albumsProjections,
            isMusic,
            null,
            sortOrderAlbum
        )
        albumQuery?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(id)
                val albumName = cursor.getString(album)?:""
                val artistName = cursor.getString(artist)?:""
                val albumArt = "content://media/external/audio/albumart/$albumId"
                albums.add(Album(albumId, albumName, artistName, albumArt))
            }
        }
        albumsListCache = albums.distinct()
        return albumsListCache
    }
    
}