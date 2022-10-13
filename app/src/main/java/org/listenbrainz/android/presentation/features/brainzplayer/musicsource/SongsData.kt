package org.listenbrainz.android.presentation.features.brainzplayer.musicsource

import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import org.listenbrainz.android.App.Companion.context
import org.listenbrainz.android.data.sources.brainzplayer.Song

class SongData {
    fun fetchSongs(): List<Song>{
        val songs = mutableListOf<Song>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val songProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
        )
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val isMusic = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val songQuery = context?.contentResolver?.query(
            collection,
            songProjection,
            isMusic,
            null,
            sortOrder
        )
        songQuery?.use { cursor ->
            val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val name = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
            val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)
            while (cursor.moveToNext()) {
                val songId = cursor.getLong(id).toString()
                val albumID = cursor.getLong(albumId)
                val songName = cursor.getString(name)
                val artistName = cursor.getString(artist)
                val albumName = cursor.getString(album) ?: ""
                val albumArt = "content://media/external/audio/albumart/$albumID"
                val contentUri = ContentUris.withAppendedId(
                    collection,
                    songId.toLong()
                ).toString()
                songs += Song(
                    mediaID = songId,
                    title = songName,
                    artist = artistName,
                    albumID = albumID.toString(),
                    album = albumName,
                    uri = contentUri,
                    albumArt = albumArt
                )
            }
        }
        return songs
    }
}