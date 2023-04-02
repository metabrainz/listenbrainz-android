package org.listenbrainz.android.ui.screens.yim

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.ui.screens.yim.navigation.YimNavigation
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModelImpl
import org.listenbrainz.android.viewmodel.YimViewModel

@AndroidEntryPoint
class YearInMusicActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val yimViewModel : YimViewModel by viewModels()
        val networkConnectivityViewModel = NetworkConnectivityViewModelImpl(this)
        
        // Login Check
        if (!yimViewModel.isLoggedIn()){
            Toast.makeText(this, "Please Login to access your Year in Music!", Toast.LENGTH_LONG).show()
            finish()
        }
        
        setContent {
            YimNavigation(yimViewModel = yimViewModel, networkConnectivityViewModel = networkConnectivityViewModel, activity = this)
        }
    }
}