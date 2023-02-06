package org.listenbrainz.android

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
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
    
    @Before
    fun setup(){
        val yimViewModel = YimViewModel(MockYimRepository())
        val networkViewModel = MockNetworkConnectivityViewModel(ConnectivityObserver.NetworkStatus.Available)
        rule.setContent {
            YimNavigation(yimViewModel = yimViewModel, activity = rule.activity, networkConnectivityViewModel = networkViewModel)
        }
    }
    @Test
    fun test(){
        rule.onNodeWithTag(rule.activity.getString(R.string.tt_yim_home_logo)).assertExists()
        rule.onNodeWithTag(rule.activity.getString(R.string.tt_yim_home_logo)).performTouchInput {
            down(center)
            moveTo(Offset(centerX,0f))
            up()
        }
        rule.waitForIdle()
        rule.onNodeWithText("Top Albums of 2022").assertExists()
        // Till Albums screen
    }
}