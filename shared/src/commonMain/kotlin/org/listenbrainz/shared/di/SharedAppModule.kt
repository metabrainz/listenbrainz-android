package org.listenbrainz.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.listenbrainz.shared.provideLogSubmitter
import org.listenbrainz.shared.provideLogger
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.LogSubmitter

val platformModule = module {
    single<LogSubmitter>{
        provideLogSubmitter(get(), buildInfo = get<BuildInfo>())
    }

    single<co.touchlab.kermit.Logger> {
        provideLogger(get(), buildInfo = get<BuildInfo>())
    }
}