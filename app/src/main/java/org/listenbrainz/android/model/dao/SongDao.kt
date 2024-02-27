package org.listenbrainz.android.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.SongEntity

@Dao
interface SongDao {
    @Query(
        value = "SELECT * FROM SONGS WHERE mediaID = :mediaId ")
    fun getSongEntity(mediaId: String) : Flow<SongEntity>

    @Query(value = "SELECT * FROM SONGS ORDER BY `title`")
    fun getSongEntities() : Flow<List<SongEntity>>

    @Query(value = "SELECT * FROM SONGS ORDER BY `title`")
    fun getSongEntitiesAsList() : List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongs(songEntities: List<SongEntity>)

    @Delete
    suspend fun deleteSong(songEntity: SongEntity)
    
}