package org.listenbrainz.android

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.ui.screens.profile.BaseProfileScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import org.listenbrainz.sharedtest.mocks.MockFeedRepository
import org.listenbrainz.sharedtest.mocks.MockListensRepository
import org.listenbrainz.sharedtest.mocks.MockRemotePlaybackHandler
import org.listenbrainz.sharedtest.mocks.MockSocialRepository
import org.listenbrainz.sharedtest.mocks.MockSocketRepository
import org.listenbrainz.sharedtest.mocks.MockUserRepository
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class UserPagesTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = createComposeRule()

    private lateinit var viewModel: UserViewModel
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var listensViewModel: ListensViewModel
    private lateinit var socialViewModel: SocialViewModel

    @Before
    fun setup() {
        val testDispatcher: TestDispatcher = StandardTestDispatcher()
        viewModel = UserViewModel(
            MockAppPreferences(),
            MockUserRepository(),
            MockListensRepository(),
            MockSocialRepository(),
            testDispatcher
        )
        feedViewModel = FeedViewModel(
            feedRepository = MockFeedRepository(),
            socialRepository = MockSocialRepository(),
            listensRepository = MockListensRepository(),
            appPreferences = MockAppPreferences(),
            remotePlaybackHandler = MockRemotePlaybackHandler(),
            testDispatcher,
            testDispatcher
        )
        listensViewModel = ListensViewModel(
            repository = MockListensRepository(),
            appPreferences = MockAppPreferences(),
            socketRepository = MockSocketRepository(),
            remotePlaybackHandler = MockRemotePlaybackHandler(),
            testDispatcher
        )
        socialViewModel = SocialViewModel(
            repository = MockSocialRepository(),
            appPreferences = MockAppPreferences(),
            remotePlaybackHandler = MockRemotePlaybackHandler(),
            testDispatcher
        )

        rule.setContent {
            runBlocking {
                viewModel.getUserDataFromRemote(testUsername)
                testDispatcher.scheduler.advanceUntilIdle()
            }
            val uiState by viewModel.uiState.collectAsState()
            ListenBrainzTheme {
                Scaffold {
                    it ->
                    BaseProfileScreen(
                        username = testUsername,
                        snackbarState = remember {
                            SnackbarHostState()
                        },
                        uiState = uiState,
                        onFollowClick = {},
                        onUnfollowClick = {},
                        goToUserProfile = { /*TODO*/ },
                        viewModel = viewModel,
                        feedViewModel = feedViewModel,
                        socialViewModel = socialViewModel,
                        listensViewModel = listensViewModel,
                    )
                }

            }
        }
    }

    @Test
    fun allTabsExistenceTest() {
        // Verify that all tabs ("Listens", "Stats", "Taste") exist on the main screen
        rule.onNodeWithText("Listens").assertExists()
        rule.onNodeWithText("Stats").assertExists()
        rule.onNodeWithText("Taste").assertExists()
    }

    @Test
    fun listensTabScreenFlowTest() {
        // Navigate to the "Listens" tab
        rule.onNodeWithText("Listens").performClick()

        // Verify that key elements on the "Listens" screen are present
        rule.onNodeWithText("You have listened to").assertExists()
        rule.onNodeWithText("Recent Listens").assertExists()
        rule.onNodeWithText("Followers").assertExists()

        // Locate the scrollable container for the "Listens" screen
        val scrollableContainer = rule.onNodeWithTag("listensScreenScrollableContainer")

        // Scroll to a specific index to ensure additional content is loaded
        scrollableContainer.performScrollToIndex(10)

        // Verify that more elements are present after scrolling
        rule.onNodeWithText("Similar Users").assertExists()
    }

    @Test
    fun statsTabScreenFlowTest() {
        // Navigate to the "Stats" tab
        rule.onNodeWithText("Stats").performClick()

        // Verify that key elements on the "Stats" screen are present
        rule.onNodeWithText("Global").assertExists()
        rule.onNodeWithText("This Week").assertExists()
        rule.onNodeWithText("This Month").assertExists()
        rule.onNodeWithText("This Year").assertExists()

        // Locate the scrollable container for the "Stats" screen
        val scrollableContainer = rule.onNodeWithTag("statsScreenScrollableContainer")

        // Scroll to a specific index to ensure additional content is loaded
        scrollableContainer.performScrollToIndex(2)

        // Verify that more elements are present after scrolling
        rule.onNodeWithText("Artists").assertExists()
        rule.onNodeWithText("Albums").assertExists()
        rule.onNodeWithText("Songs").assertExists()

        // Scroll further to check the presence of additional content
        scrollableContainer.performScrollToIndex(3)
        rule.onNodeWithText("Load More").assertExists()
    }

    @Test
    fun tasteTabScreenFlowTest() {
        // Navigate to the "Taste" tab
        rule.onNodeWithText("Taste").performClick()

        // Verify that key elements on the "Taste" screen are present
        rule.onNodeWithText("Loved").assertExists()
        rule.onNodeWithText("Hated").assertExists()
        rule.onNodeWithText("Pins").assertExists()
    }

}
