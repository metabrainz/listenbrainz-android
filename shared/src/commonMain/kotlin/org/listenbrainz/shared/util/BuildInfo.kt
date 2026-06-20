package org.listenbrainz.shared.util

data class BuildInfo(
    val applicationId: String,
    val versionCode: Int,
    val versionName: String,
    val buildType: String
)
