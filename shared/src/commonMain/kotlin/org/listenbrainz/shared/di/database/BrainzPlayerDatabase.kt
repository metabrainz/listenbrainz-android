package org.listenbrainz.shared.di.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.listenbrainz.shared.model.AlbumEntity
import org.listenbrainz.shared.model.ArtistEntity
import org.listenbrainz.shared.model.PlaylistEntity
import org.listenbrainz.shared.model.SongEntity
import org.listenbrainz.shared.model.dao.AlbumDao
import org.listenbrainz.shared.model.dao.ArtistDao
import org.listenbrainz.shared.model.dao.PlaylistDao
import org.listenbrainz.shared.model.dao.SongDao
import org.listenbrainz.shared.util.TypeConverter

object Migrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            val query = connection.prepare("PRAGMA table_info('SONGS')")
            var columnExists = false
            try {
                while (query.step()){
                    val columnName = query.getText(1)
                    if(columnName == "lastListenedTo"){
                        columnExists = true
                        break
                    }
                }
            } finally {
                query.close()
            }
            if (!columnExists) {
                connection.execSQL(
                    "ALTER TABLE 'SONGS' ADD COLUMN 'lastListenedTo' INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
    val MIGRATION_2_3: Migration = object : Migration(2,3){
        override fun migrate(connection: SQLiteConnection){
            connection.execSQL("DROP TABLE IF EXISTS `PLAYLISTS_TEMP`")
            connection.execSQL("""
                CREATE TABLE `PLAYLISTS_TEMP` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL DEFAULT '',
                    `items` TEXT NOT NULL,
                    `art` TEXT NOT NULL DEFAULT 'ic_queue_music'
                )
            """.trimIndent()
            )
            connection.execSQL("""
                INSERT INTO `PLAYLISTS_TEMP` (`id`,`title`,`items`,`art`)
                SELECT 
                    `id`,
                    `title`,
                    `items`,
                    CASE
                        WHEN `id` = -1 THEN 'ic_queue_music_playing'
                        WHEN `id` = 0 THEN 'ic_liked'
                        ELSE 'ic_queue_music'
                    END
                FROM `PLAYLISTS`
            """.trimIndent())

            connection.execSQL("DROP TABLE `PLAYLISTS`")
            connection.execSQL("ALTER TABLE `PLAYLISTS_TEMP` RENAME TO `PLAYLISTS`")
            connection.execSQL("""
                INSERT OR REPLACE INTO sqlite_sequence (name,seq)
                VALUES ('PLAYLISTS', (SELECT MAX(id) FROM `PLAYLISTS`))
            """.trimIndent())
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
    version = 3,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
@ConstructedBy(BrainzPlayerDatabaseConstructor::class)
abstract class BrainzPlayerDatabase : RoomDatabase() {
    abstract fun songDao() : SongDao
    abstract fun albumDao() : AlbumDao
    abstract fun artistDao() : ArtistDao
    abstract fun playlistDao() : PlaylistDao
}


@Suppress("KotlinNoActualForExpect")
expect object BrainzPlayerDatabaseConstructor: RoomDatabaseConstructor<BrainzPlayerDatabase>{
    override fun initialize(): BrainzPlayerDatabase
}