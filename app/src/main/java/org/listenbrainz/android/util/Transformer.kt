package org.listenbrainz.android.util

import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.AlbumEntity
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.ArtistEntity
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.model.PlaylistEntity
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.SongEntity

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
        songs = emptyList(),
        albums = emptyList()
    )

    fun Artist.toArtistEntity() = ArtistEntity(
        artistID = id,
        name = name,
        songs = songs.map {
            it.toSongEntity()
        },
        albums = albums.map {
            it.toAlbumEntity()
        }
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