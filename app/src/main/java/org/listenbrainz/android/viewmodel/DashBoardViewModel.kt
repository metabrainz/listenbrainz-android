package org.listenbrainz.android.viewmodel

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.ui.screens.onboarding.PermissionEnum
import org.listenbrainz.android.util.Log
import javax.inject.Inject

@HiltViewModel
class DashBoardViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val application: Application,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    val usernameFlow = appPreferences.username.getFlow()
    val permissionStatusFlow = MutableStateFlow(emptyMap<PermissionEnum, PermissionStatus>())

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
        
    fun getPermissionStatus(activity: ComponentActivity){
        val requiredPermissions = PermissionEnum.getRequiredPermissionsList()
        val permissionMap = mutableMapOf<PermissionEnum, PermissionStatus>()
        requiredPermissions.forEach { permission->
            if(permission.isGranted(activity)){
                permissionMap[permission] = PermissionStatus.GRANTED
            }else if(permission.isPermissionPermanentlyDeclined(activity)){
                permissionMap[permission] = PermissionStatus.DENIED_TWICE
            }else{
                permissionMap[permission] = PermissionStatus.NOT_REQUESTED
            }
        }
        viewModelScope.launch {
            permissionStatusFlow.emit(permissionMap)
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