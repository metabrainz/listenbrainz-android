package org.listenbrainz.android.repository.brainzplayer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.shared.model.Song
import org.listenbrainz.shared.model.dao.SongDao
import org.listenbrainz.android.util.SongsData
import org.listenbrainz.android.util.Transformer.toSong
import org.listenbrainz.android.util.Transformer.toSongEntity
import kotlin.time.Clock

class SongRepositoryImpl(
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

    override suspend fun updateSong(song : Song) {
        songDao.updateSong(song.toSongEntity())
    }

    override fun getRecentlyPlayedSongs(): Flow<List<Song>> =
        songDao.getRecentlyPlayedSongs()
            .map { it ->
                it.map{
                    it.toSong()
                }
            }

    override fun getSongsPlayedToday(): Flow<List<Song>>  {
        val now = Clock.System.now().toEpochMilliseconds()
        return songDao.getSongsPlayedToday(now)
            .map { it ->
                it.map {
                    it.toSong()
                }
            }
    }

    override fun getSongsPlayedThisWeek(): Flow<List<Song>> {
        val now = Clock.System.now().toEpochMilliseconds()
        return songDao.getSongsPlayedThisWeek(now)
            .map { it ->
                it.map {
                    it.toSong()
                }
            }
    }
}