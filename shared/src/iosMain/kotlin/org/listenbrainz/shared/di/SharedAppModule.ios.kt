package org.listenbrainz.shared.di

import org.koin.dsl.module
import org.listenbrainz.shared.provideLogSubmitter
import org.listenbrainz.shared.provideLogger
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.IosLogSubmitter
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.LogSubmitter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual val platformModule = module {
    single<LogSubmitter> {
        provideLogSubmitter(context = PlatformContext(), buildInfo = get<BuildInfo>())
    }
    single<co.touchlab.kermit.Logger> {
        val logDirectory = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        ).firstOrNull() as String
        provideLogger(logDirectory,get())
            .also {
                Log.sharedLogger = it
            }
    }
}