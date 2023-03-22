package org.listenbrainz.android.ui.screens.login

import android.os.Bundle
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.AppPreferencesImpl
import org.listenbrainz.android.ui.components.ListenBrainzActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.LoginViewModel

@AndroidEntryPoint
class LoginActivity : ListenBrainzActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        viewModel.appPreferences = AppPreferencesImpl(this)

        setContent {
            ListenBrainzTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
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
            }
        }

        if (intent?.data?.getQueryParameter("code") != null) {
            val code = intent.data!!.getQueryParameter("code")!!
            viewModel.fetchAccessToken(code)
        } else {
            viewModel.startLogin(this)
        }
    }
}