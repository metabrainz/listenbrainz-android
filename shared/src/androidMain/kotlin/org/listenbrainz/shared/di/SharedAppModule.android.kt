package org.listenbrainz.shared.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.listenbrainz.shared.provideLogSubmitter
import org.listenbrainz.shared.provideLogger
import org.listenbrainz.shared.util.AndroidLogSubmitter
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.LogSubmitter

actual val platformModule = module {
    single<LogSubmitter> {
        provideLogSubmitter(androidContext(), buildInfo = get<BuildInfo>())
    }

    single<co.touchlab.kermit.Logger> {
        val context = androidContext()
        val logDirectory = context.getExternalFilesDir(null)?.path
        provideLogger(logDirectory, buildInfo = get<BuildInfo>())
            .also {
                Log.sharedLogger = it
            }
    }

}