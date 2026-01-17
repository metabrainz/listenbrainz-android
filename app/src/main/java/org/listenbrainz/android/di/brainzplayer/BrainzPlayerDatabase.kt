package org.listenbrainz.android.di.brainzplayer

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.listenbrainz.android.model.AlbumEntity
import org.listenbrainz.android.model.ArtistEntity
import org.listenbrainz.android.model.PlaylistEntity
import org.listenbrainz.android.model.SongEntity
import org.listenbrainz.android.model.dao.AlbumDao
import org.listenbrainz.android.model.dao.ArtistDao
import org.listenbrainz.android.model.dao.PlaylistDao
import org.listenbrainz.android.model.dao.SongDao
import org.listenbrainz.android.util.TypeConverter

object Migrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val cursor = db.query("PRAGMA table_info('SONGS')")
            var columnExists = false
            val columnNameIndex = cursor.getColumnIndex("name")
            if (columnNameIndex != -1) {
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(columnNameIndex)
                    if (columnName == "lastListenedTo") {
                        columnExists = true
                        break
                    }
                }
            }
            cursor.close()

            if (!columnExists) {
                db.execSQL(
                    "ALTER TABLE 'SONGS' ADD COLUMN 'lastListenedTo' INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class BrainzPlayerDatabase : RoomDatabase() {
    abstract fun songDao() : SongDao
    abstract fun albumDao() : AlbumDao
    abstract fun artistDao() : ArtistDao
    abstract fun playlistDao() : PlaylistDao
}