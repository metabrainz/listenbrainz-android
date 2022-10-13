package org.listenbrainz.android.presentation

import android.content.Context
import android.content.Intent
import org.listenbrainz.android.presentation.features.settings.SettingsActivity

object IntentFactory {
    fun getSettings(context: Context?): Intent {
        return Intent(context, SettingsActivity::class.java)
    }
}