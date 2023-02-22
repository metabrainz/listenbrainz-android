package org.listenbrainz.android.data.repository

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.data.dao.ArtistDao
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toAlbumEntity
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toArtist
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toArtistEntity
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toSongEntity
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Artist
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.AlbumsData
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.SongsData
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

    override suspend fun addArtists(userRequestedRefresh: Boolean): Boolean {
        val artists = AlbumsData.fetchAlbums(userRequestedRefresh)
            .map {
                it.toArtistEntity()
            }
            .distinct()
        
        // Deleting artists that are not present in the device anymore.
        artistDao.getArtistEntitiesAsList().forEach { artistEntity ->
            if (!artists.contains(artistEntity))
                artistDao.deleteArtist(artistEntity.name)
        }
        
        lateinit var songsJob : Deferred<Unit>
        lateinit var albumsJob : Deferred<Unit>
        
        withContext(Dispatchers.IO){
            // Both jobs are being executed simultaneously.
            songsJob = async {
                for (artist in artists) {
                    // Here, if userRequestedRefresh is true, it will refresh songs cache which is what we expect from refreshing
                    artist.songs.addAll(addAllSongsOfArtist(artist.toArtist(), userRequestedRefresh).map {
                        it.toSongEntity()
                    })
                }
            }
            albumsJob = async {
                for (artist in artists) {
                    // We do not need to refresh cache (songsListCache) here as it already got refreshed above when we created list of albums.
                    artist.albums.addAll(addAllAlbumsOfArtist(artist.toArtist()).map {
                        it.toAlbumEntity()
                    })
                }
            }
        }
        
        songsJob.await()
        albumsJob.await()
        
        artistDao.addArtists(artists)
        return artists.isNotEmpty()
    }

    override suspend fun addAllSongsOfArtist(artist: Artist, userRequestedRefresh: Boolean): List<Song> {
        return SongsData.fetchSongs(userRequestedRefresh).filter {
            it.artist == artist.name
        }
            .map {
                it
            }
    }

    override suspend fun addAllAlbumsOfArtist(artist: Artist): List<Album> {
        return AlbumsData.fetchAlbums().filter {
            it.artist == artist.name
        }
            .map {
                it
            }
    }
}