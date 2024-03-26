package org.listenbrainz.android.di.brainzplayer

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBrainzPlayerDatabase(
        @ApplicationContext context: Context
    ) : BrainzPlayerDatabase = Room.databaseBuilder(
        context,
        BrainzPlayerDatabase::class.java,
        "brainzplayer_database"
    )
        .addMigrations(MIGRATION_1_2)
        .build()
    
    @Provides
    @Singleton
    fun providesListenSubmissionDatabase(
        @ApplicationContext context: Context
    ): ListensSubmissionDatabase = Room.databaseBuilder(
        context,
        ListensSubmissionDatabase::class.java,
        "listens_scrobble_database"    // TODO: change this later
    )
        .build()
}
