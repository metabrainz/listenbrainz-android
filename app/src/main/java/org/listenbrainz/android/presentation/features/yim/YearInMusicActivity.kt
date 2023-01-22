package org.listenbrainz.android.presentation.features.yim

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.presentation.features.login.LoginActivity
import org.listenbrainz.android.presentation.features.yim.navigation.YimNavigation

@AndroidEntryPoint
class YearInMusicActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val networkViewModel = YimNetworkViewModel(this)
        val yimViewModel : YimViewModel by viewModels()
        
        // Login Check
        if (!yimViewModel.isLoggedIn()){
            Toast.makeText(this, "Please Login to access your Year in Music!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        setContent {
            YimNavigation(yimViewModel = yimViewModel, networkViewModel = networkViewModel, activity = this)
        }
    }
}