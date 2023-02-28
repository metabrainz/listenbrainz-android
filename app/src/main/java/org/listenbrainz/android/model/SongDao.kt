package org.listenbrainz.android.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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