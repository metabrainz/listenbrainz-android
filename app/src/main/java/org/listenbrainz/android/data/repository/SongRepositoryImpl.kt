package org.listenbrainz.android.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.listenbrainz.android.data.dao.SongDao
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toSong
import org.listenbrainz.android.data.di.brainzplayer.Transformer.toSongEntity
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.presentation.features.brainzplayer.musicsource.SongData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao)
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