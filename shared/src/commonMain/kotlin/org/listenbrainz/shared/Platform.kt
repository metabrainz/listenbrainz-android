package org.listenbrainz.shared

import co.touchlab.kermit.Logger

expect fun platform(): String

expect fun provideLogger(): Logger