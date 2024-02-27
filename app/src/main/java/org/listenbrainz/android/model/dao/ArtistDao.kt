package org.listenbrainz.android.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.ArtistEntity

@Dao
interface ArtistDao {
    @Query(value = "SELECT * FROM ARTISTS WHERE artistID = :artistID")
    fun getArtistEntity(artistID: String) : Flow<ArtistEntity>
    
    @Query(value = "SELECT * FROM ARTISTS ORDER BY `name`")
    fun getArtistEntities() : Flow<List<ArtistEntity>>
    
    @Query(value = "SELECT * FROM ARTISTS ORDER BY `name`")
    fun getArtistEntitiesAsList() : List<ArtistEntity>
    
    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun addArtists(artistEntities: List<ArtistEntity>)//: List<Long>
    
    /*@Query(value = "DELETE FROM ARTISTS WHERE name = :artistName")
    suspend fun deleteArtist(artistName: String)*/
    
    @Query(value = "DELETE FROM ARTISTS")
    suspend fun deleteAllArtists()
}