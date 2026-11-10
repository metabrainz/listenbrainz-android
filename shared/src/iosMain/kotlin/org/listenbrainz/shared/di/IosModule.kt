package org.listenbrainz.shared.di

import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.AppPreferencesImpl
import org.listenbrainz.shared.repository.IosPlatformContext
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import platform.Foundation.NSBundle

/** What androidApp's Koin modules provide on Android. */
val iosModule = module {
    single<PlatformContext> { IosPlatformContext }
    single<AppPreferences> { AppPreferencesImpl(get()) }
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            encodeDefaults = true
            explicitNulls = false
        }
    }
    single<BuildInfo> {
        val info = NSBundle.mainBundle.infoDictionary.orEmpty()
        BuildInfo(
            applicationId = NSBundle.mainBundle.bundleIdentifier.orEmpty(),
            versionCode = (info["CFBundleVersion"] as? String)?.toIntOrNull() ?: 0,
            versionName = info["CFBundleShortVersionString"] as? String ?: "",
            buildType = if (BuildKonfig.DEBUG) "debug" else "release"
        )
    }
}

fun ensureKoinStarted() {
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        modules(
            iosModule,
            sharedDispatcherModule,
            sharedViewModelModule,
            sharedNetworkServiceModule,
            sharedRepositoryModule,
            platformModule,
            sharedDatabaseModule,
            sharedDaoModule
        )
    }
}
