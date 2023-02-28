package org.listenbrainz.android.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query(value = "SELECT * FROM ALBUMS WHERE albumId = :albumId")
    fun getAlbumEntity(albumId: Long): Flow<AlbumEntity>

    @Query(value = "SELECT * FROM ALBUMS")
    fun getAlbumEntities(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlbums(albumEntities: List<AlbumEntity>)
}