package org.listenbrainz.android.model.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.AlbumEntity

@Dao
interface AlbumDao {
    
    @Query(value = "SELECT * FROM ALBUMS WHERE albumId = :albumId")
    fun getAlbumEntity(albumId: Long): Flow<AlbumEntity>
    
    @Query(value = "SELECT * FROM ALBUMS ORDER BY `title`")
    fun getAlbumEntities(): Flow<List<AlbumEntity>>
    
    @Query(value = "SELECT * FROM ALBUMS ORDER BY `title`")
    fun getAlbumEntitiesAsList(): List<AlbumEntity>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlbums(albumEntities: List<AlbumEntity>)
    
    /*@Query("DELETE FROM ALBUMS")
    suspend fun deleteAllAlbums()*/
    
    @Delete
    suspend fun deleteAlbum(albumEntity: AlbumEntity)
}