package org.listenbrainz.android.feed

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.setExplicitContent
import org.listenbrainz.android.ui.screens.feed.FeedScreen
import org.listenbrainz.android.ui.screens.feed.FeedUiState

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class FeedFlowTest {
    
    @get:Rule(order = 0)
    val rule = createAndroidComposeRule<ComponentActivity>()
    
    @Before
    fun setup(){
        
        rule.setExplicitContent {
            
            FeedScreen(
                uiState = FeedUiState(),
                scrollToTopState = false,
                onScrollToTop = {},
                onDeleteOrHide = { feedEvent: FeedEvent, feedEventType: FeedEventType, s: String -> },
                onErrorShown = {},
                recommendTrack = {},
                personallyRecommendTrack = { feedEvent: FeedEvent, strings: List<String>, s: String -> },
                review = { feedEvent: FeedEvent, reviewEntityType: ReviewEntityType, s: String, i: Int?, s1: String -> },
                pin = { feedEvent: FeedEvent, s: String? -> },
                searchFollower = {},
                isCritiqueBrainzLinked = { true },
                onPlay = {}
            )
        }
    }
    
    @Test
    fun feedScreenTest() = runTest {
    
    }
}