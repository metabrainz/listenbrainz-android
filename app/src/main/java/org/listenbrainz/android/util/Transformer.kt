package org.listenbrainz.android.util

import org.listenbrainz.android.model.*

object Transformer {
    fun SongEntity.toSong() = Song(
        mediaID = mediaID,
        title = title,
        artist = artist,
        uri = uri,
        albumID = albumID,
        album = album,
        albumArt = albumArt,
        artistId = artistId,
        dateModified = dateModified,
        trackNumber = trackNumber,
        duration = duration,
        discNumber = discNumber,
        year = year
    )

    fun Song.toSongEntity() = SongEntity(
        mediaID = mediaID,
        title = title,
        artist = artist,
        uri = uri,
        albumID = albumID,
        album = album,
        albumArt = albumArt,
        artistId = artistId,
        dateModified = dateModified,
        trackNumber = trackNumber,
        duration = duration,
        discNumber = discNumber,
        year = year
    )

    fun AlbumEntity.toAlbum() = Album(
        albumId = albumId,
        title = title,
        artist = artist,
        albumArt = albumArt
    )

    fun Album.toAlbumEntity() = AlbumEntity(
        albumId = albumId,
        title = title,
        artist = artist,
        albumArt = albumArt
    )

    fun Album.toArtistEntity() = ArtistEntity(
        name = artist,
        songs = mutableListOf(),
        albums = mutableListOf()
    )

    fun Artist.toArtistEntity() = ArtistEntity(
        artistID = id,
        name = name,
        songs = songs.map {
            it.toSongEntity()
        }.toMutableList(),
        albums = albums.map {
            it.toAlbumEntity()
        }.toMutableList()
    )

    fun ArtistEntity.toArtist() = Artist(
        id = artistID,
        name = name,
        songs = songs.map {
            it.toSong()
        },
        albums = albums.map {
            it.toAlbum()
        }
    )

    fun Playlist.toPlaylistEntity() = PlaylistEntity(
        id = id,
        title = title,
        items = items.map {
            it.toSongEntity()
        },
        art = art
    )

    fun PlaylistEntity.toPlaylist() = Playlist(
        id = id,
        title = title,
        items = items.map {
            it.toSong()
        },
        art = art
    )
}