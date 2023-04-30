package org.listenbrainz.android.ui.screens.settings

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.application.App
import org.listenbrainz.android.databinding.ActivityPreferencesBinding
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.theme.isUiModeIsDark
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_ENABLED
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_THEME
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.app_bg)))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment(appPreferences))
                .commit()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            ACTION_NOTIFICATION_LISTENER_SETTINGS = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        }
    
        val preferenceChangeListener: Preference.OnPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
            if (preference.key == PREFERENCE_LISTENING_ENABLED) {
                val enabled = newValue as Boolean
                when {
                    enabled && !appPreferences.isNotificationServiceAllowed -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Grant Media Control Permissions")
                        builder.setMessage("The listen service requires the special Notification " +
                                "Listener Service Permission to run. Please grant this permission to" +
                                " ListenBrainz for Android if you want to use the service.")
                        builder.setPositiveButton("Proceed") { dialog: DialogInterface?, which: Int ->
                            startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->
                            appPreferences.preferenceListeningEnabled = false
                            (preference as SwitchPreference).isChecked = false
                        }
                        builder.create().show()
                    }
                    !enabled -> App.context!!.stopListenService()
                }
                return@OnPreferenceChangeListener true
            }
            
            // Explicit Ui Mode functionality.
            if (preference.key == PREFERENCE_SYSTEM_THEME){
                val intent = Intent(this@SettingsActivity, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                when (newValue) {
                    "Dark" -> {
                        setDefaultNightMode(MODE_NIGHT_YES)
                        isUiModeIsDark.value = true
                    }
                    "Light" -> {
                        setDefaultNightMode(MODE_NIGHT_NO)
                        isUiModeIsDark.value = false
                    }
                    else -> {
                        setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                        isUiModeIsDark.value = null
                    }


                }
                startActivity(intent)
                finish()
                return@OnPreferenceChangeListener true
            }
            false
        }
    
        // Attaching OnPreferenceChangeListener to our settings fragment.
        supportFragmentManager.addFragmentOnAttachListener { fragmentManager, fragment ->
            if (fragment is SettingsFragment)
                fragment.setPreferenceChangeListener(preferenceChangeListener)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private var ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    }
}