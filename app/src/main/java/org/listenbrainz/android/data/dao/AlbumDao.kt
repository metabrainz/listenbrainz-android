package org.listenbrainz.android.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.AlbumEntity

@Dao
interface AlbumDao {

    @Query(value = "SELECT * FROM ALBUMS WHERE albumId = :albumId")
    fun getAlbumEntity(albumId: Long): Flow<AlbumEntity>

    @Query(value = "SELECT * FROM ALBUMS")
    fun getAlbumEntities(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAlbums(albumEntities: List<AlbumEntity>)
}