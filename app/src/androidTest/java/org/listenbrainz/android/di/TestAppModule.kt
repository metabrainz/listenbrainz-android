package org.listenbrainz.android.di

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManagerImpl
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.sharedtest.mocks.MockAppPreferences

val testAppModule = module {
    single<AppPreferences> { MockAppPreferences() }
    
    single<WorkManager> {
        val context: Context = get()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        WorkManager.getInstance(context)
    }
    
    single<BrainzPlayerServiceConnection> {
        BrainzPlayerServiceConnection(get(), get(), get())
    }
    
    single<ListenServiceManager> {
        ListenServiceManagerImpl(get(), get(), get())
    }
    
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
}
