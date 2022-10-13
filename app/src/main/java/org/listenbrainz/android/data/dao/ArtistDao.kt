package org.listenbrainz.android.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.data.sources.brainzplayer.ArtistEntity

@Dao
interface ArtistDao {
    @Query(value = "SELECT * FROM ARTISTS WHERE artistID = :artistID")
    fun getArtistEntity(artistID: String) : Flow<ArtistEntity>

    @Query(value = "SELECT * FROM ARTISTS")
    fun getArtistEntities() : Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addArtists(artistEntities: List<ArtistEntity>): List<Long>

}