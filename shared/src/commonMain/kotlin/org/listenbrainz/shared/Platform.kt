package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import org.listenbrainz.shared.util.BuildInfo

expect fun platform(): String

expect fun provideLogger(
    logFileDirectory:String?=null,
    buildInfo: BuildInfo?
): Logger