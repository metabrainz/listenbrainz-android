package org.listenbrainz.android.presentation.features.yim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.presentation.features.yim.navigation.YimNavigation
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModelImpl

@AndroidEntryPoint
class YearInMusicActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val yimViewModel : YimViewModel by viewModels()
        val networkConnectivityViewModel = NetworkConnectivityViewModelImpl(this)
        
        // Login Check
        /*if (!yimViewModel.isLoggedIn()){
            Toast.makeText(this, "Please Login to access your Year in Music!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }*/
        
        setContent {
            YimNavigation(yimViewModel = yimViewModel, networkConnectivityViewModel = networkConnectivityViewModel, activity = this)
        }
    }
}