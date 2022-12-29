package org.listenbrainz.android.presentation.features.yim

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.presentation.features.login.LoginActivity
import org.listenbrainz.android.presentation.features.yim.screens.YimHomeScreen
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme

@AndroidEntryPoint
class YearInMusicActivity : ComponentActivity() {
    
    
    @RequiresApi(Build.VERSION_CODES.N)     // For observe
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel : YimViewModel by viewModels()
        
        // Login Check
        if (!viewModel.isLoggedIn()){
            Toast.makeText(this, "Please Login to access your Year in Music!", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        setContent {
            
            val status = viewModel.getNetworkStatus()
            val data = viewModel.getYimData().data
            
            YearInMusicTheme {
                YimHomeScreen(viewModel = viewModel)
            }
        }
    }
}