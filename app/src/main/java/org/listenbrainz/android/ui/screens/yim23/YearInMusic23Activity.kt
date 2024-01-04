package org.listenbrainz.android.ui.screens.yim23

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.ui.screens.yim23.navigation.Yim23Navigation
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModelImpl
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

@AndroidEntryPoint
class YearInMusic23Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val yim23ViewModel : Yim23ViewModel by viewModels()
        val socialViewModel : SocialViewModel by viewModels()
        val networkConnectivityViewModel: NetworkConnectivityViewModel =
            ViewModelProvider(this)[NetworkConnectivityViewModelImpl::class.java]

        setContent {
            val loginStatus by yim23ViewModel.loginFlow.collectAsState(initial = STATUS_LOGGED_IN)
            // Login Check
            if (loginStatus == STATUS_LOGGED_OUT){
                Toast.makeText(this, "Please Login to access your Year in Music!",
                    Toast.LENGTH_LONG).show()
                finish()
            }
            Yim23Navigation(yimViewModel = yim23ViewModel, socialViewModel = socialViewModel
                , networkConnectivityViewModel = networkConnectivityViewModel, activity = this)
        }
    }
}