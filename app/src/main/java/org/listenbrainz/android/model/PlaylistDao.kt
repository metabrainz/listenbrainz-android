package org.listenbrainz.android.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM PLAYLISTS")
    fun getAllPlaylist(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM PLAYLISTS WHERE id LIKE :playlistId")
    fun getPlaylist(playlistId: Long): Flow<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlistEntity: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlistEntities: List<PlaylistEntity>)

    @Query("UPDATE playlists SET items =:songs WHERE id =:playlistId")
    suspend fun updatePlaylistSongs(songs:List<SongEntity>, playlistId: Long)

    @Query("UPDATE playlists SET title=:newName WHERE id =:playlistId")
    suspend fun renamePlaylistName(newName: String, playlistId: Long)

    @Delete
    suspend fun delete(playlistEntity: PlaylistEntity)

}