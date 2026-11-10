package org.listenbrainz.shared.permission

import android.content.Context
import android.content.Intent

internal sealed interface AndroidPermissionSpec {
    val minSdk: Int

    class Runtime(val manifestPermission: String, override val minSdk: Int) : AndroidPermissionSpec

    class SpecialAccess(
        override val minSdk: Int,
        val isGranted: (Context) -> Boolean,
        val settingsIntent: (Context) -> Intent
    ) : AndroidPermissionSpec
}
