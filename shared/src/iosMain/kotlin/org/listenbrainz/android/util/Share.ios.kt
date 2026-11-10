package org.listenbrainz.android.util

actual fun shareLink(link: String) = presentShareSheet(listOf(link))
