package org.listenbrainz.shared.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.getListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao

val sharedDatabaseModule = module {
    single<ListensSubmissionDatabase>{
        getListensSubmissionDatabase(appContext = get())
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

val sharedDaoModule = module {
    single<PendingListensDao>{
        get<ListensSubmissionDatabase>().pendingListensDao()
    }
}