package org.listenbrainz.android.model.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.SongEntity

@Dao
interface SongDao {
    @Query(
        value = "SELECT * FROM SONGS WHERE mediaID = :mediaId ")
    fun getSongEntity(mediaId: String) : Flow<SongEntity>

    @Query(value = "SELECT * FROM SONGS")
    fun getSongEntities() : Flow<List<SongEntity>>

    @Query(value = "SELECT * FROM SONGS")
    fun getSongEntitiesAsList() : List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongs(songEntities: List<SongEntity>)

    @Delete
    suspend fun deleteSong(songEntity: SongEntity)
    
}