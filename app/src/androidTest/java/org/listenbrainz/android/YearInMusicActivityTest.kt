package org.listenbrainz.android

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimNavigation
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.sharedtest.mocks.MockNetworkConnectivityViewModel
import org.listenbrainz.sharedtest.mocks.MockYimRepository

@LargeTest
@RunWith(AndroidJUnit4::class)
class YearInMusicActivityTest {
    
    @get:Rule(order = 0)
    val rule = createAndroidComposeRule<ComponentActivity>()
    
    private lateinit var activity : ComponentActivity
    
    @Before
    fun setup(){
        activity = rule.activity
        val yimViewModel = YimViewModel(MockYimRepository())
        val networkViewModel = MockNetworkConnectivityViewModel(ConnectivityObserver.NetworkStatus.Available)
        rule.setContent {
            YimNavigation(yimViewModel = yimViewModel, activity = activity, networkConnectivityViewModel = networkViewModel)
        }
    }
    @Test
    fun screenFlowTest(){
        
        verifyExistence(R.string.tt_yim_home_logo)
        rule.onNodeWithTag(activity.getString(R.string.tt_yim_home_logo)).performTouchInput {
            down(bottomCenter)
            moveTo(topCenter)
            up()
        }
        
        rule.onNodeWithText("Top Albums of 2022").assertExists()
        nextPage()
        
        verifyExistence(R.string.tt_yim_charts_heading)
        scrollToEnd(R.string.tt_yim_charts_parent)
        nextPage()
        
        verifyExistence(R.string.tt_yim_statistics_heading)
        nextPage()
        
        verifyExistence(R.string.tt_yim_recommended_playlists_heading)
        scrollToEnd(R.string.tt_yim_recommended_playlists_parent)
        nextPage()
        
        verifyExistence(R.string.tt_yim_discover_heading)
        scrollToEnd(R.string.tt_yim_discover_parent)
        nextPage()
        
        verifyExistence(R.string.tt_yim_endgame_heading)
    }
    
    private fun scrollToEnd(@StringRes stringRes: Int){
        rule.onNodeWithTag(activity.getString(stringRes)).performTouchInput {
            down(bottomRight)
            moveTo(topRight)
            up()
        }
    }
    
    private fun verifyExistence(@StringRes stringRes: Int){
        rule.onNodeWithTag(activity.getString(stringRes)).assertExists()
    }
    
    private fun nextPage(){
        rule.onNodeWithTag(activity.getString(R.string.tt_yim_next_button)).performClick()
    }
}