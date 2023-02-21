package org.listenbrainz.android.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.data.dao.AlbumDao
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toAlbum
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toAlbumEntity
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.AlbumsData
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.SongsData
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao
): AlbumRepository {
    override fun getAlbums(): Flow<List<Album>> =
        albumDao.getAlbumEntities()
            .map { it ->
                it.map {
                    it.toAlbum()
                }
            }

    override fun getAlbum(albumId: Long): Flow<Album> =
        albumDao.getAlbumEntity(albumId)
            .map { it.toAlbum() }


    override suspend fun addAlbums(userRequestedRefresh: Boolean): Boolean {
        val albums = AlbumsData.fetchAlbums(userRequestedRefresh).map {
            it.toAlbumEntity()
        }
        
        // This helps us remove those albums that don't exist anymore.
        albumDao.getAlbumEntitiesAsList().forEach {
            if (!albums.contains(it))
                albumDao.deleteAlbum(it)
        }
        // Adding new albums
        albumDao.addAlbums(albums)
        
        /*  Fixes a case where there are multiple songs in an album, removing one song might not affect
            the result of the album as we are caching the songs in it.   */
        if (userRequestedRefresh){
            /*  This task is threaded to provide greater efficiency as
                no immediate result is required from it.    */
            withContext(Dispatchers.IO){
                // Update cache (companion object) of SongsData
                SongsData.updateCache()
            }
        }
        return albums.isNotEmpty()
    }

    override fun getAllSongsOfAlbum(albumId: Long): Flow<List<Song>> {
        val songs = SongsData.fetchSongs().filter { song ->
            song.albumID == albumId
        }
        return flowOf(songs)
    }
}