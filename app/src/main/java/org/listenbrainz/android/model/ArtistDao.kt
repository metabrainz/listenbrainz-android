package org.listenbrainz.android.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query(value = "SELECT * FROM ARTISTS WHERE artistID = :artistID")
    fun getArtistEntity(artistID: String) : Flow<ArtistEntity>

    @Query(value = "SELECT * FROM ARTISTS")
    fun getArtistEntities() : Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addArtists(artistEntities: List<ArtistEntity>): List<Long>

}