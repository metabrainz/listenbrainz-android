package org.listenbrainz.android.ui.screens.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ListenBrainzTheme {
                SettingsScreen {
                    finish()
                }
            }
        }
    }
}