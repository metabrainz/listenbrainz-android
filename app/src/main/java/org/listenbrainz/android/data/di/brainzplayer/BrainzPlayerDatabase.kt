package org.listenbrainz.android.data.di.brainzplayer

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.listenbrainz.android.data.dao.AlbumDao
import org.listenbrainz.android.data.dao.ArtistDao
import org.listenbrainz.android.data.dao.PlaylistDao
import org.listenbrainz.android.data.dao.SongDao
import org.listenbrainz.android.data.sources.brainzplayer.AlbumEntity
import org.listenbrainz.android.data.sources.brainzplayer.ArtistEntity
import org.listenbrainz.android.data.sources.brainzplayer.PlaylistEntity
import org.listenbrainz.android.data.sources.brainzplayer.SongEntity

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