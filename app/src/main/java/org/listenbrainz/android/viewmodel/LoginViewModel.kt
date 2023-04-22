package org.listenbrainz.android.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.LoginRepository
import org.listenbrainz.android.repository.LoginRepository.Companion.errorToken
import org.listenbrainz.android.repository.LoginRepository.Companion.errorUserInfo
import org.listenbrainz.android.util.ListenBrainzServiceGenerator
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    val appPreferences: AppPreferences
) : ViewModel() {
    
    /** Initial value: **null** */
    val accessTokenFlow: Flow<AccessToken?> = repository.accessTokenFlow
    
    /** Initial value: **null** */
    val userInfoFlow: Flow<UserInfo?> = repository.userInfoFlow

    private fun fetchAccessToken(code: String) {
        repository.fetchAccessToken(code)
    }

    private fun fetchUserInfo() {
        repository.fetchUserInfo()
    }
    
    /** Starts the web-view and controls redirection from website.*/
    fun checkRedirectUri(callbackUri: Uri?) : Int {
        with(callbackUri){
            if (this != null && this.toString().startsWith(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI)) {
                
                if (getQueryParameter("error") != null){
                    // User denies access
                    return R.string.login_request_denied
                    
                } else if (getQueryParameter("code") != null) {
                    // User provides access
                    fetchAccessToken(getQueryParameter("code")!!)
                    return R.string.login_request_approved
                }else {
                    return R.string.login_failed
                }
    
            }else {
                // No redirects started this activity, which means user initiated login.
                return R.string.login_started
            }
        }
    }
    
    
    fun getStartLoginIntent() : Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                ListenBrainzServiceGenerator.AUTH_BASE_URL
                        + "authorize"
                        + "?response_type=code"
                        + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                        + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                        + "&scope=profile%20collection%20tag%20rating"
            )
        )
    }
    
    /**Should be called using [accessTokenFlow] as follows:
     * ```
     *  viewModel.accessTokenFlow.collectLatest { accessToken: AccessToken? ->
     *      if (accessToken != null)
     *          val response = viewModel.saveOAuthToken(accessToken)
     *          // do something with response
     *      }
     *  }
     * ```
     *
     * Automatically calls [fetchUserInfo] which updates [userInfoFlow].
     * @param accessToken should be **non-null**.
     * @return [Int] which points to a string resource. */
    fun saveOAuthToken(accessToken: AccessToken) : Int {
        return when (accessToken) {
            errorToken -> {
                // Login failed because the app could not fetch access token.
                R.string.login_failed_access_token
            }
            else -> {
                appPreferences.saveOAuthToken(accessToken)
                fetchUserInfo()     // UserInfo flow is then updated which finishes the activity.
                R.string.login_success_access_token
            }
        }
    }
    
    /** Should be called using [userInfoFlow] as follows:
     * ```
     *   viewModel.userInfoFlow.collectLatest { userInfo: UserInfo? ->
     *      if (userInfo != null) {
     *          val response = viewModel.saveUserInfo(userInfo)
     *          // do something with response
     *      }
     *   }
     *   ```
     * @param userInfo should be **non-null**.
     * @return [Int] which points to a string resource.*/
    fun saveUserInfo(userInfo: UserInfo) : Int {
        return when (userInfo) {
            errorUserInfo -> {
                // Remove access token.
                appPreferences.logoutUser()
                R.string.login_failed_user_info
            }
            else -> {
                // Save user info.
                appPreferences.saveUserInfo(userInfo)
                R.string.login_success_user_info
            }
        }
    }
    
}