package org.listenbrainz.android

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity

@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
class YearInMusicActivityTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<YearInMusicActivity>()
    
    @Before
    fun init(){
        hiltRule.inject()
    }
    
    @Test
    fun startupTest(){
        // TODO: Find fix
        /*rule.setContent {
            val yvm: YimViewModel = viewModel()
            val nvm: NetworkConnectivityViewModel = viewModel()
            
            YimNavigation(yimViewModel = yvm, activity = rule.activity, networkConnectivityViewModel = nvm)
        }*/
    }
}