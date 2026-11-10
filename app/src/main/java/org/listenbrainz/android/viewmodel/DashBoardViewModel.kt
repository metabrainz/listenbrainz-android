package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginConsentScreenUIState
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.onboarding.OnboardingState
import org.listenbrainz.shared.permission.PermissionManager
import org.listenbrainz.shared.permission.PermissionScope
import org.listenbrainz.shared.permission.missing
import org.listenbrainz.shared.util.LogSubmitter

data class DashBoardUiState(
    val username: String? = null,
    val navBarOrder: List<AppNavigationItem>? = null,
    val onBoardingCompleted: Boolean = false,
    val consentScreenUIState: LoginConsentScreenUIState = LoginConsentScreenUIState(),
    val isSubmittingLogs: Boolean = false
)

class DashBoardViewModel(
    val appPreferences: AppPreferences,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    private val logSubmitter: LogSubmitter,
    private val permissionManager: PermissionManager,
    private val logger: Log = Log
) : ViewModel() {

    /** Null until the stored values have loaded. */
    val onboardingState: StateFlow<OnboardingState?> = combine(
        appPreferences.onboardingCompleted.getFlow(),
        appPreferences.lbAccessToken.getFlow(),
        permissionManager.statuses
    ) { isOnboardingCompleted, accessToken, statuses ->
        OnboardingState(
            isOnboardingCompleted = isOnboardingCompleted,
            isUserLoggedIn = accessToken.isNotEmpty(),
            isPermissionMissing = statuses
                .missing(permissionManager.permissionsFor(PermissionScope.ONBOARDING))
                .isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val usernameFlow = appPreferences.username.getFlow()

    private val navBarOrderFlow = appPreferences.navBarOrder
        .getFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _onboardingCompletedState = appPreferences.onboardingCompleted.getFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _consentScreenUIState = MutableStateFlow(LoginConsentScreenUIState())

    private val _submittingLogs = MutableStateFlow(false)

    val uiState: StateFlow<DashBoardUiState> = combine(
        usernameFlow,
        navBarOrderFlow,
        _onboardingCompletedState,
        _consentScreenUIState,
        _submittingLogs
    ) { flows ->
        DashBoardUiState(
            username = flows[0] as? String,
            navBarOrder = flows[1] as? List<AppNavigationItem>,
            onBoardingCompleted = flows[2] as Boolean,
            consentScreenUIState = flows[3] as LoginConsentScreenUIState,
            isSubmittingLogs = flows[4] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DashBoardUiState()
    )

    init {
        viewModelScope.launch {
            val cacheData = appPreferences.consentScreenDataCache.getFlow().first()
            if (cacheData.isNotEmpty()) {
                _consentScreenUIState.update { uIState ->
                    uIState.copy(
                        data = cacheData,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun logSubmit() {
        if (_submittingLogs.value) {
            return
        }
        viewModelScope.launch {
            _submittingLogs.value = true
            try {
                logSubmitter.submitLogs()
            } catch (e: Exception) {
                logger.e("Unable to submit logs: $e")
            } finally {
                _submittingLogs.value = false
            }
        }
    }

    fun changeConsentScreenUIState(consentScreenUIState: LoginConsentScreenUIState) {
        _consentScreenUIState.update {
            consentScreenUIState
        }
        if (consentScreenUIState.data?.isNotEmpty() == true) {
            viewModelScope.launch {
                appPreferences.consentScreenDataCache.set(consentScreenUIState.data)
            }
        }
    }

    private fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            appPreferences.onboardingCompleted.set(completed)
        }
    }

    fun markOnboardingCompleted() {
        setOnboardingCompleted(true)
    }

    fun resetOnboardingCompleted() {
        setOnboardingCompleted(false)
    }

    fun isOnboardingCompleted(): Boolean = _onboardingCompletedState.value

    fun onLoadConsentScreen() {
        _consentScreenUIState.update { it ->
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }
        _consentScreenUIState.value.webView?.loadUrl("https://listenbrainz.org/login")
    }

    suspend fun isNotificationListenerServiceAllowed() =
        appPreferences.isNotificationServiceAllowed
                && appPreferences.isListeningAllowed.get()
                && appPreferences.lbAccessToken.get().isNotEmpty()


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
