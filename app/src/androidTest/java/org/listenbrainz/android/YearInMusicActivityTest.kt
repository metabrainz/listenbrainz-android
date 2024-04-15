package org.listenbrainz.android

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.screens.yim.navigation.YimNavigation
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.viewmodel.YimViewModel
import org.listenbrainz.sharedtest.mocks.MockNetworkConnectivityViewModel
import org.listenbrainz.sharedtest.mocks.MockYimRepository
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
//@LargeTest
@HiltAndroidTest
class YearInMusicActivityTest {
    
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Mock
    private lateinit var mockAppPreferences: AppPreferences
    
    private lateinit var activity : ComponentActivity

    @Before
    fun setup(){
        activity = rule.activity
        val yimViewModel = YimViewModel(
            MockYimRepository(), mockAppPreferences
        )
        val networkViewModel = MockNetworkConnectivityViewModel(ConnectivityObserver.NetworkStatus.AVAILABLE)

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
        nextPage(scrollToEnd = false)

        verifyExistence(R.string.tt_yim_charts_heading)
        nextPage()

        verifyExistence(R.string.tt_yim_statistics_heading)
        nextPage()

        verifyExistence(R.string.tt_yim_recommended_playlists_heading)
        nextPage()

        verifyExistence(R.string.tt_yim_discover_heading)
        nextPage()

        verifyExistence(R.string.tt_yim_endgame_heading)
    }

    private fun verifyExistence(@StringRes stringRes: Int){
        rule.onNodeWithTag(activity.getString(stringRes)).assertExists()
    }

    private fun nextPage(scrollToEnd: Boolean = true){
        rule.waitForIdle()
        rule.onNodeWithTag(activity.getString(R.string.tt_yim_next_button)).apply {
            if (scrollToEnd){
                performScrollTo()
            }
            performClick()
        }
    }
    
    private fun initMocks() {
        
    }
}
