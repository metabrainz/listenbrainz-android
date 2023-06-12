package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.dao.SongDao
import org.listenbrainz.android.util.SongsData
import org.listenbrainz.android.util.Transformer.toSong
import org.listenbrainz.android.util.Transformer.toSongEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {
    override fun getSongsStream(): Flow<List<Song>> =
        songDao.getSongEntities()
            .map { it ->
                it.map {
                    it.toSong()
                }
            }
    
    override suspend fun addSongs(userRequestedRefresh: Boolean): Boolean {
        val songs = SongsData.fetchSongs(userRequestedRefresh).map {
            it.toSongEntity()
        }
        
        // This helps us remove those songs that don't exist anymore.
        songDao.getSongEntitiesAsList().forEach {
            if (!songs.contains(it))
                songDao.deleteSong(it)
        }
        
        // Adding new songs
        songDao.addSongs(songs)
        
        return songs.isNotEmpty()
    }
}