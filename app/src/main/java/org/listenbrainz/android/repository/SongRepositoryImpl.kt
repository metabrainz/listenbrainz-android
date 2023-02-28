package org.listenbrainz.android.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.model.SongDao
import org.listenbrainz.android.util.Transformer.toSong
import org.listenbrainz.android.util.Transformer.toSongEntity
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.util.SongData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
)
    : SongRepository {
    override fun getSongsStream(): Flow<List<Song>> =
        songDao.getSongEntities()
            .map { it ->
                it.map {
                    it.toSong()
                }
            }

    override suspend fun addSongs(): Boolean {
         val songs = SongData().fetchSongs().map {
            it.toSongEntity()
        }
        songDao.addSongs(songs)
        return songs.isNotEmpty()
    }
}