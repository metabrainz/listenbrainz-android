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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
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

    val permissionsRequestedAteastOnce = appPreferences.requestedPermissionsList.getFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


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

        
    fun getPermissionStatus(activity: ComponentActivity){
        viewModelScope.launch(ioDispatcher) {
            val requiredPermissions = PermissionEnum.getRequiredPermissionsList()
            val permissionMap = mutableMapOf<PermissionEnum, PermissionStatus>()
            val permissionsReqeustedOnce = appPreferences.requestedPermissionsList.getFlow().first()
            requiredPermissions.forEach { permission ->
                if (permission.isGranted(activity)) {
                    permissionMap[permission] = PermissionStatus.GRANTED
                    //This is to ensure that the permission is marked as requested (for devices which already gave permission before any prompt)
                    markPermissionAsRequested(permission)
                } else if (permission.isPermissionPermanentlyDeclined(
                        activity,
                        permissionsReqeustedOnce
                    )
                ) {
                    permissionMap[permission] = PermissionStatus.DENIED_TWICE
                } else {
                    permissionMap[permission] = PermissionStatus.NOT_REQUESTED
                }
            }
            permissionStatusFlow.emit(permissionMap)

        }
    }

    fun markPermissionAsRequested(permission: PermissionEnum) {
        viewModelScope.launch(ioDispatcher) {
            val permissions = appPreferences.requestedPermissionsList.getFlow().firstOrNull()?.toMutableList()
            if (permissions != null && !permissions.contains(permission.permission)) {
                permissions.add(permission.permission)
                appPreferences.requestedPermissionsList.set(permissions)
            }
        }
    }

    fun markOnboardingComplete(){
        viewModelScope.launch(ioDispatcher) {
            appPreferences.onboardingCompleted = true
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