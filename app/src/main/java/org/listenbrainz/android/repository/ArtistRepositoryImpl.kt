package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.ArtistDao
import org.listenbrainz.android.util.Transformer.toAlbumEntity
import org.listenbrainz.android.util.Transformer.toArtist
import org.listenbrainz.android.util.Transformer.toArtistEntity
import org.listenbrainz.android.util.Transformer.toSongEntity
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.util.AlbumsData
import org.listenbrainz.android.util.SongData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val artistDao: ArtistDao
    ) : ArtistRepository {
    override fun getArtist(artistID: String): Flow<Artist> {
        val artist = artistDao.getArtistEntity(artistID)
        return artist.map {
            it.toArtist()
        }
    }

    override fun getArtists(): Flow<List<Artist>> =
        artistDao.getArtistEntities()
            .map { it ->
                it.map {
                    it.toArtist()
                }
            }

    override suspend fun addArtists(): Boolean {
        val artists = AlbumsData().fetchAlbums()
            .map {
                it.toArtistEntity()
            }
            .distinct()
        for (artist in artists) {
            artist.songs.addAll(addAllSongsOfArtist(artist.toArtist()).map {
                it.toSongEntity()
            })
            artist.albums.addAll(addAllAlbumsOfArtist(artist.toArtist()).map {
                it.toAlbumEntity()
            })
        }
        artistDao.addArtists(artists)
        return artists.isNotEmpty()
    }

    override suspend fun addAllSongsOfArtist(artist: Artist): List<Song> {
        return SongData().fetchSongs().filter {
            it.artist == artist.name
        }
            .map {
                it
            }
    }

    override suspend fun addAllAlbumsOfArtist(artist: Artist): List<Album> {
        return AlbumsData().fetchAlbums().filter {
            it.artist == artist.name
        }
            .map {
                it
            }
    }
}