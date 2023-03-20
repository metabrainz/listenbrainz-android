package org.listenbrainz.android.ui.screens.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.AppPreferencesImpl
import org.listenbrainz.android.ui.components.ListenBrainzActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.LBSharedPreferences.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.ListenBrainzServiceGenerator
import org.listenbrainz.android.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginActivity : ListenBrainzActivity() {

    private lateinit var viewModel: LoginViewModel
    var isIntentLaunched : Boolean? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    
        // Controls launch of startLogin
        isIntentLaunched = intent.getBooleanExtra("startLogin", false)
        
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        viewModel.appPreferences = AppPreferencesImpl(this)
        
        setContent {
            ListenBrainzTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.inverseOnSurface)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Authenticating",
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
                        viewModel.saveOAuthToken(accessToken, this@LoginActivity)
                    }
                }
                launch {
                    viewModel.userInfoFlow.collectLatest { userInfo: UserInfo? ->
                        viewModel.saveUserInfo(userInfo, this@LoginActivity)
                    }
                }
                launch {
                    // Login session Timeout
                    if (!(isIntentLaunched as Boolean)){
                        delay(5000)
                        Toast.makeText(this@LoginActivity, "Login failed, please try again.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
        
        if (isIntentLaunched as Boolean){
            viewModel.startLogin(this)
        }
        
    }

    override fun onResume() {
    
        /*var loginStatus by Delegates.notNull<Int>()
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            loginStatus = if (
                getString(MB_ACCESS_TOKEN, "")?.isNotEmpty() == true &&
                getString(USERNAME, "")?.isNotEmpty() == true
            ){
                STATUS_LOGGED_IN
            }else{
                STATUS_LOGGED_OUT
            }
        }*/
        
        if (viewModel.appPreferences.loginStatus == STATUS_LOGGED_OUT){
        //if (loginStatus == STATUS_LOGGED_OUT){
            val callbackUri = intent.data
            if (callbackUri != null && callbackUri.toString().startsWith(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI)) {
                val code = callbackUri.getQueryParameter("code")
                if (code != null) {
                    viewModel.fetchAccessToken(code)
                }
            }
        }
        super.onResume()
    }
}