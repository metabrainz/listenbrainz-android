package org.listenbrainz.android.viewmodel

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

@HiltViewModel
class DashBoardViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val application: Application,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    // Sets Ui mode for XML layouts.
    fun setUiMode(){
        viewModelScope.launch {
            when(withContext(ioDispatcher) {
                appPreferences.themePreference.get()
            } ){
                UiMode.DARK -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
                UiMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    
    suspend fun getPermissionsPreference(): String? =
        withContext(ioDispatcher) {
            appPreferences.permissionsPreference
        }
    
    fun setPermissionsPreference(value: String?) =
        viewModelScope.launch(ioDispatcher) {
            appPreferences.permissionsPreference = value
        }
        
    
    
    fun beginOnboarding(activity: ComponentActivity) {
        d("Onboarding status: ${appPreferences.onboardingCompleted}")
        if (!appPreferences.onboardingCompleted){
            // TODO: Convert onboarding to a nav component.
            activity.startActivity(Intent(activity, FeaturesActivity::class.java))
            activity.finish()
        }
    }
    
    // Permissions required by the app.
    // TODO: Rework permissions
    val neededPermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else -> {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    // Update permission preference
    // If the user enables permission from settings, this function updates the preference.
    fun updatePermissionPreference(){
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (
                    checkSelfPermission(application.applicationContext, Manifest.permission.READ_MEDIA_IMAGES) == PermissionChecker.PERMISSION_GRANTED &&
                    checkSelfPermission(application.applicationContext, Manifest.permission.READ_MEDIA_AUDIO) == PermissionChecker.PERMISSION_GRANTED
                ){
                    setPermissionsPreference(PermissionStatus.GRANTED.name)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (checkSelfPermission(application.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                    setPermissionsPreference(PermissionStatus.GRANTED.name)
                }
            }
            else -> {
                if (when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                            checkSelfPermission(application.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED &&
                                    checkSelfPermission(application.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED
                        }
                        else -> {
                            checkSelfPermission(application.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED &&
                                    checkSelfPermission(application.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED
                        }
                    }
                ){
                    setPermissionsPreference(PermissionStatus.GRANTED.name)
                }
            }
        }
    }
    
    suspend fun isNotificationListenerServiceAllowed(): Boolean {
        return withContext(ioDispatcher) {
            appPreferences.isNotificationServiceAllowed
                && appPreferences.isListeningAllowed.get()
        } && appPreferences.lbAccessToken.get().isNotEmpty()
    }
    
    fun connectToSpotify() {
        viewModelScope.launch {
            remotePlaybackHandler.connectToSpotify {
                // TODO: Propagate error to UI
            }
        }
    }
    
    fun disconnectSpotify() {
        viewModelScope.launch { remotePlaybackHandler.disconnectSpotify() }
    }
}