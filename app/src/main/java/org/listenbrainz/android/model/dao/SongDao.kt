package org.listenbrainz.android.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query(value = "SELECT * FROM SONGS ORDER BY `lastListenedTo` DESC")
    fun getRecentlyPlayedSongs() : Flow<List<SongEntity>>
    @Query(value = "SELECT * FROM SONGS WHERE (:currentTime - lastListenedTo*1000) < 86400000 ORDER BY (:currentTime - lastListenedTo*1000) ASC")
    fun getSongsPlayedToday(
        currentTime : Long = System.currentTimeMillis()
    ) : Flow<List<SongEntity>>
    @Query(value = "SELECT * FROM SONGS WHERE :currentTime - lastListenedTo > 86400000 AND :currentTime - lastListenedTo < 604800000  ORDER BY :currentTime - lastListenedTo ASC")
    fun getSongsPlayedThisWeek(
        currentTime : Long = System.currentTimeMillis()
    ) : Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongs(songEntities: List<SongEntity>)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSong(songEntity: SongEntity)

    @Delete
    suspend fun deleteSong(songEntity: SongEntity)
    
}