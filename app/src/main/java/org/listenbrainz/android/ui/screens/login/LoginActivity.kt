package org.listenbrainz.android.ui.screens.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.ui.components.ListenBrainzActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginActivity : ListenBrainzActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        
        // Variable used to show progress of login process.
        var loginState by mutableStateOf(R.string.login_uninitialized)
    
        val response = viewModel.checkRedirectUri(callbackUri = intent.data)
        when (response){
            R.string.login_started -> {
                
                startActivity(viewModel.getStartLoginIntent())
                // Finish the activity so that if user returns to the app without any action,
                // he/she should not be stuck in this activity.
                finish()
            }
            R.string.login_request_denied -> {
                Toast.makeText(this, getString(R.string.login_request_denied), Toast.LENGTH_SHORT).show()
                finish()
            }
            R.string.login_failed_server_error -> {
                Toast.makeText(this, getString(R.string.login_failed_server_error), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        // Updating loginState
        loginState = response
        
        
        setContent {
            ListenBrainzTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.inverseOnSurface)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = loginState),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.accessTokenFlow.collectLatest { accessToken: AccessToken? ->
                        // null means no login request has been made.
                        if (accessToken != null){
                            val accessTokenResponse = viewModel.saveOAuthToken(accessToken)
                            
                            // Updating loginState
                            loginState = accessTokenResponse
    
                            if (accessTokenResponse == R.string.login_failed_access_token) {
                                Toast.makeText(this@LoginActivity, getString(accessTokenResponse), Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            
                        }
                    }
                }
                launch {
                    viewModel.userInfoFlow.collectLatest { userInfo: UserInfo? ->
                        if (userInfo != null) {
                            val userInfoResponse = viewModel.saveUserInfo(userInfo)
                            
                            // Updating loginState
                            loginState = userInfoResponse
                            
                            if (userInfoResponse == R.string.login_failed_user_info){
                                Toast.makeText(this@LoginActivity, userInfoResponse, Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this@LoginActivity, "Login successful. " + userInfo.username + " is now logged in.", Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }
    

}