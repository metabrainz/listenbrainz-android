package org.listenbrainz.android.ui.screens.yim

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.ui.screens.yim.navigation.YimNavigation
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModelImpl
import org.listenbrainz.android.viewmodel.YimViewModel

@AndroidEntryPoint
class YearInMusicActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val yimViewModel : YimViewModel by viewModels()
        val networkConnectivityViewModel: NetworkConnectivityViewModel =
            ViewModelProvider(this)[NetworkConnectivityViewModelImpl::class.java]
        
        setContent {
            val loginStatus by yimViewModel.loginFlow.collectAsState(initial = STATUS_LOGGED_IN)
            // Login Check
            if (loginStatus == STATUS_LOGGED_OUT){
                Toast.makeText(this, "Please Login to access your Year in Music!", Toast.LENGTH_LONG).show()
                finish()
            }
            YimNavigation(yimViewModel = yimViewModel, networkConnectivityViewModel = networkConnectivityViewModel, activity = this)
        }
    }
}