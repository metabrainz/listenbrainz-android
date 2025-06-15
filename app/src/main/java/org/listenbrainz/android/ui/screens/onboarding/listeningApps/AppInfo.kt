package org.listenbrainz.android.ui.screens.onboarding.listeningApps

import android.graphics.Bitmap

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Bitmap,
    val isWhitelisted: Boolean
)
