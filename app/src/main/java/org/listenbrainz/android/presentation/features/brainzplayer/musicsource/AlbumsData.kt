package org.listenbrainz.android.presentation.features.brainzplayer.musicsource

import android.os.Build
import android.provider.MediaStore
import androidx.preference.PreferenceManager
import org.listenbrainz.android.App.Companion.context
import org.listenbrainz.android.data.sources.brainzplayer.Album
import javax.inject.Singleton

class AlbumsData {
    fun fetchAlbums(): List<Album> {
        // If there aren't any albums on the device.
        if (!albumsOnDevice){
            return emptyList()
        }
        if(albumsList.isNotEmpty()){
            return albumsList
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
        val albumQuery = context?.contentResolver?.query(
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
        albumsList = albums.distinct()
        // This means there are no albums on the device.
        if (albumsList.isEmpty()){
            albumsOnDevice = false
        }
        return albumsList
    }
    
    @Singleton
    companion object {
        private var albumsList = listOf<Album>()
        const val PREFERENCE_ALBUMS_ON_DEVICE = "PREFERENCE_ALBUMS_ON_DEVICE"
        var albumsOnDevice       // Used for showing progress indicators.
            get() = PreferenceManager.getDefaultSharedPreferences(context!!).getBoolean(PREFERENCE_ALBUMS_ON_DEVICE, true)
            set(value) {
                PreferenceManager.getDefaultSharedPreferences(context!!)
                    .edit()
                    .putBoolean(PREFERENCE_ALBUMS_ON_DEVICE, value)
                    .apply()
            }
    }
}