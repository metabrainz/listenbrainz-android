package org.listenbrainz.shared.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.dsl.module
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.di.database.Migrations
import org.listenbrainz.shared.getBrainzPlayerDatabase
import org.listenbrainz.shared.model.dao.AlbumDao
import org.listenbrainz.shared.model.dao.ArtistDao
import org.listenbrainz.shared.model.dao.PlaylistDao
import org.listenbrainz.shared.model.dao.SongDao

val sharedDatabaseModule = module {
    single<BrainzPlayerDatabase> {
        getBrainzPlayerDatabase(get())
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addMigrations(Migrations.MIGRATION_1_2)
            .build()
    }
}

val sharedDaoModule = module {
    single<SongDao>{
        get<BrainzPlayerDatabase>().songDao()
    }
    single<PlaylistDao>{
        get<BrainzPlayerDatabase>().playlistDao()
    }
    single<AlbumDao>{
        get<BrainzPlayerDatabase>().albumDao()
    }
    single<ArtistDao>{
        get<BrainzPlayerDatabase>().artistDao()
    }
}