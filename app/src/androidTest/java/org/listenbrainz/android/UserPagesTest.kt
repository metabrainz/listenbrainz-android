package org.listenbrainz.android

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.profile.listens.ListensScreen
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.UserViewModel
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import org.listenbrainz.sharedtest.mocks.MockListensRepository
import org.listenbrainz.sharedtest.mocks.MockSocialRepository
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

        rule.setContent {
            runBlocking {
                viewModel.getUserDataFromRemote(testUsername)
                testDispatcher.scheduler.advanceUntilIdle()
            }
            val uiState by viewModel.uiState.collectAsState()
            ListenBrainzTheme {
                ListensScreen(
                    scrollRequestState = false,
                    onScrollToTop = {},
                    username = testUsername,
                    uiState = uiState,
                    feedUiState = FeedUiState(),
                    preferencesUiState = PreferencesUiState(),
                    updateNotificationServicePermissionStatus = { /*TODO*/ },
                    dropdownItemIndex = remember {
                        mutableStateOf(null)
                    },
                    validateUserToken = {_, -> true},
                    setToken = {},
                    playListen = {},
                    snackbarState = remember {
                        SnackbarHostState()
                    },
                    socialUiState = SocialUiState(),
                    onRecommend = {},
                    onErrorShown = { /*TODO*/ },
                    onMessageShown = { /*TODO*/ },
                    onPin = {_, _ ->},
                    searchUsers = {},
                    isCritiqueBrainzLinked = {true},
                    onReview = {_, _, _, _, _ ->},
                    onPersonallyRecommend = {_, _, _ ->},
                    onFollowButtonClick = {_, _ ->}
                )
            }
        }
    }

    @Test
    fun listensTabScreenFlowTest() {
        rule.onNodeWithText("You have listened to").assertExists()
        rule.onNodeWithText("Recent Listens").assertExists()
        rule.onNodeWithText("Followers").assertExists()
        val scrollableContainer = rule.onNodeWithTag("listensScreenScrollableContainer")
        scrollableContainer.performScrollToIndex(10)
        rule.onNodeWithText("Similar Users").assertExists()
//        rule.waitUntil (
//            timeoutMillis = 5000L,
//            condition = {false}
//        )
    }
}
