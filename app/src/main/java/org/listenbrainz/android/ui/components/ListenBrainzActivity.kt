package org.listenbrainz.android.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_LANGUAGE
import org.listenbrainz.android.util.IntentFactory.getSettings
import org.listenbrainz.android.util.Utils.changeLanguage
import org.listenbrainz.android.util.Utils.emailIntent

abstract class ListenBrainzActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dash, menu)
        return true
    }

    protected open fun getBrowserURI(): Uri? {
        return Uri.EMPTY
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.menu_preferences -> {
                startActivity(getSettings(applicationContext))
            }
            R.id.menu_feedback -> {
                sendFeedback()
            }
            R.id.menu_features -> {
                startActivity(Intent(this, FeaturesActivity::class.java))
            }
            R.id.menu_open_website -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = getBrowserURI()
                startActivity(intent)
            }
        }
        return false
    }

    override fun attachBaseContext(newBase: Context) {
        if (PreferenceManager.getDefaultSharedPreferences(newBase).getBoolean(PREFERENCE_SYSTEM_LANGUAGE, false)) {
            val context: Context = changeLanguage(newBase, "en")
            super.attachBaseContext(context)
        } else super.attachBaseContext(newBase)
    }

    private fun sendFeedback() {
        try {
            startActivity(emailIntent(Constants.FEEDBACK_EMAIL, Constants.FEEDBACK_SUBJECT))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_feedback_fail, Toast.LENGTH_LONG).show()
        }
    }
}