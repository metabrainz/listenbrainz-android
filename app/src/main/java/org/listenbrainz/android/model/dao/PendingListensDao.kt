package org.listenbrainz.android.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.listenbrainz.android.model.ListenSubmitBody

@Dao
interface PendingListensDao {
    
    @Query("SELECT * FROM PENDING_LISTENS")
    fun getPendingListens(): List<ListenSubmitBody.Payload>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addListen(payload: ListenSubmitBody.Payload)
    
    @Delete
    fun deleteListens(vararg listens: ListenSubmitBody.Payload)
    
    @Query("DELETE FROM PENDING_LISTENS")
    fun deleteAllPendingListens()
}