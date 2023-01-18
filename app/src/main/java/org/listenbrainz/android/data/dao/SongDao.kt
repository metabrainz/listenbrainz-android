package org.listenbrainz.android.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.SongEntity

@Dao
interface SongDao {
    @Query(
        value = "SELECT * FROM SONGS WHERE mediaID = :mediaId ")
    fun getSongEntity(mediaId: String) : Flow<SongEntity>

    @Query(value = "SELECT * FROM SONGS")
    fun getSongEntities() : Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongs(songEntities: List<SongEntity>)
    
}