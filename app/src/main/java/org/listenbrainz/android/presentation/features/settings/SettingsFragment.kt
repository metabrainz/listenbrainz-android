package org.listenbrainz.android.presentation.features.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.UserPreferences.PREFERENCE_LISTENING_ENABLED
import org.listenbrainz.android.presentation.UserPreferences.PREFERENCE_SYSTEM_THEME
import org.listenbrainz.android.presentation.UserPreferences.preferenceListeningEnabled
import org.listenbrainz.android.presentation.features.dashboard.DashboardActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var preferenceChangeListener: Preference.OnPreferenceChangeListener? = null

    fun setPreferenceChangeListener(preferenceChangeListener: Preference.OnPreferenceChangeListener?) {
        this.preferenceChangeListener = preferenceChangeListener
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>(PREFERENCE_SYSTEM_THEME)!!.onPreferenceChangeListener = preferenceChangeListener
        if (!App.context!!.isNotificationServiceAllowed) {
            (findPreference<Preference>(PREFERENCE_LISTENING_ENABLED) as SwitchPreference?)!!.isChecked = false
            preferenceListeningEnabled = false
        }
        findPreference<Preference>(PREFERENCE_LISTENING_ENABLED)!!.onPreferenceChangeListener = preferenceChangeListener
    }

    override fun onResume() {
        super.onResume()
        if (!App.context!!.isNotificationServiceAllowed) {
            (findPreference<Preference>(PREFERENCE_LISTENING_ENABLED) as SwitchPreference?)!!.isChecked = false
            preferenceListeningEnabled = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(context, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            else -> false
        }
    }
}