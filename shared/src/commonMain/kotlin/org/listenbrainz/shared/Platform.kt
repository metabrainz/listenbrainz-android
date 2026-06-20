package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter

expect fun platform(): String

expect fun provideLogger(
    context: PlatformContext,
    buildInfo: BuildInfo
): Logger

expect fun provideLogSubmitter(
    context: PlatformContext,
    buildInfo: BuildInfo
): LogSubmitter