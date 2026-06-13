package org.listenbrainz.shared.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.di.database.Migrations
import org.listenbrainz.shared.getBrainzPlayerDatabase
import org.listenbrainz.shared.model.dao.AlbumDao
import org.listenbrainz.shared.model.dao.ArtistDao
import org.listenbrainz.shared.model.dao.PlaylistDao
import org.listenbrainz.shared.model.dao.SongDao
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.getListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao

val sharedDatabaseModule = module {
    single<BrainzPlayerDatabase> {
        getBrainzPlayerDatabase(get())
            .setDriver(BundledSQLiteDriver())
            .addMigrations(Migrations.MIGRATION_1_2,Migrations.MIGRATION_2_3)
            .build()
    }

    single<ListensSubmissionDatabase>{
        getListensSubmissionDatabase(appContext = get())
            .setDriver(BundledSQLiteDriver())
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
    single<PendingListensDao>{
        get<ListensSubmissionDatabase>().pendingListensDao()
    }
}