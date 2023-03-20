package org.listenbrainz.android.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.LoginRepository
import org.listenbrainz.android.repository.LoginRepository.Companion.errorToken
import org.listenbrainz.android.repository.LoginRepository.Companion.errorUserInfo
import org.listenbrainz.android.ui.screens.login.findActivity
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.ListenBrainzServiceGenerator
import org.listenbrainz.android.util.Log.d
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {
    
    lateinit var appPreferences: AppPreferences
    
    private val _accessTokenFlow: MutableStateFlow<AccessToken?> = repository.accessTokenFlow
    val accessTokenFlow: Flow<AccessToken?> = _accessTokenFlow
    
    private val _userInfoFlow: MutableStateFlow<UserInfo?> = repository.userInfoFlow
    val userInfoFlow: Flow<UserInfo?> = _userInfoFlow

    fun fetchAccessToken(code: String) {
        repository.fetchAccessToken(code)
    }

    private fun fetchUserInfo() {
        repository.fetchUserInfo()
    }

    fun getLoginStatusFlow(): Flow<Int> {
        return flow {
            while (true){
                kotlinx.coroutines.delay(200)
                emit(appPreferences.loginStatus)
            }
        }.distinctUntilChanged()
    }
    
    fun startLogin(context: Context) {
        val intent = Intent(
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
        context.startActivity(intent)
    }

    fun saveOAuthToken(
        accessToken: AccessToken?,
        context: Context
    ) {
        when {
            accessToken == errorToken -> {
                Toast.makeText(
                    context,
                    "Failed to obtain access token.",
                    Toast.LENGTH_LONG
                ).show()
                _accessTokenFlow.update { null }
            }
            accessToken != null -> {
                LBSharedPreferences.saveOAuthToken(accessToken)
                fetchUserInfo()     // UserInfo flow is then updated which finishes the activity.
            }
            // null means no login request has been made.
        }
    }
    
    
    fun saveUserInfo(userInfo: UserInfo?, context: Context) {
        when {
            userInfo == errorUserInfo -> {
                d("Could not save user info.")
                Toast.makeText(context, "Login Unsuccessful", Toast.LENGTH_SHORT).show()
                _userInfoFlow.update { null }
                context.findActivity()?.finish()
            }
            userInfo != null && appPreferences.loginStatus == LBSharedPreferences.STATUS_LOGGED_OUT -> {
                appPreferences.saveUserInfo(userInfo)
                Toast.makeText(
                    context,
                    "Login successful. " + userInfo.username + " is now logged in.",
                    Toast.LENGTH_LONG
                ).show()
                context.findActivity()?.finish()
            }
        }
    }
    
    fun logoutUser(context: Context) {
        appPreferences.logoutUser()
        Toast.makeText(
            context,
            "User has successfully logged out.",
            Toast.LENGTH_LONG
        ).show()
        _accessTokenFlow.update { null }
        _userInfoFlow.update { null }
    }
}