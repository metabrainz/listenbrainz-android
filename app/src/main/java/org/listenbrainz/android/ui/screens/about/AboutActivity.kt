package org.listenbrainz.android.ui.screens.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ListenBrainzTheme {
                AboutScreen(version = appPreferences.version) {
                    finish()
                }
            }
        }
    }
}