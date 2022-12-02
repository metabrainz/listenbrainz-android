package org.listenbrainz.android.presentation.theme

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import org.listenbrainz.android.presentation.UserPreferences

class ThemeViewModel : ViewModel() {
    private val _theme = MutableLiveData(UserPreferences.PREFERENCE_SYSTEM_THEME)
    val theme: LiveData<String> = _theme
    
    fun onThemeChanged(isDarkTheme: Boolean?) {
        when (isDarkTheme) {
            null -> _theme.value = "Use device theme"
            false -> _theme.value = "Light"
            true -> _theme.value = "Dark"
        }
    }
    
    /*private fun userSelectedThemeIsNight() : Boolean? {
        return when (PreferenceManager.getDefaultSharedPreferences()
            .getString("app_theme", "Use device theme")){   // R.string.settings_device_theme_use_device_theme
            "Dark" -> true
            "Light" -> false
            else -> null
        }
    }*/
}

// Instance of my ViewModel.
object ThemeView {
    //lateinit var viewModel : ThemeViewModel
}