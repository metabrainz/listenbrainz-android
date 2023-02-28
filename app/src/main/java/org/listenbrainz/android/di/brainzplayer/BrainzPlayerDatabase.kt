package org.listenbrainz.android.di.brainzplayer

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.listenbrainz.android.model.AlbumDao
import org.listenbrainz.android.model.ArtistDao
import org.listenbrainz.android.model.PlaylistDao
import org.listenbrainz.android.model.SongDao
import org.listenbrainz.android.model.AlbumEntity
import org.listenbrainz.android.model.ArtistEntity
import org.listenbrainz.android.model.PlaylistEntity
import org.listenbrainz.android.model.SongEntity
import org.listenbrainz.android.util.TypeConverter

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class
    ],
    version = 1
)
@TypeConverters(TypeConverter::class)
abstract class BrainzPlayerDatabase : RoomDatabase() {
    abstract fun songDao() : SongDao
    abstract fun albumDao() : AlbumDao
    abstract fun artistDao() : ArtistDao
    abstract fun playlistDao() : PlaylistDao
}