package org.listenbrainz.android.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.ui.screens.profile.stats.StatsRange
import org.listenbrainz.android.ui.screens.profile.stats.UserGlobal
import org.listenbrainz.android.viewmodel.UserViewModel
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import org.listenbrainz.sharedtest.mocks.MockListensRepository
import org.listenbrainz.sharedtest.mocks.MockSocialRepository
import org.listenbrainz.sharedtest.mocks.MockUserRepository
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

class UserViewModelTest {
    private lateinit var viewModel: UserViewModel
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup(){
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = UserViewModel(MockAppPreferences(), MockUserRepository(), MockListensRepository(), MockSocialRepository(), Dispatchers.Default)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getDataTest() = runTest {
        // Start the data retrieval process using the ViewModel
        viewModel.getUserDataFromRemote(testUsername)

        // Ensure all coroutines and background tasks are completed before assertions
        advanceUntilIdle()

        // Check that the statsTabUIState is not null, meaning data has been loaded
        assertNotNull(viewModel.uiState.value.statsTabUIState)

        // Check that the tasteTabUIState is not null, indicating successful data load
        assertNotNull(viewModel.uiState.value.tasteTabUIState)

        // Ensure the listensTabUiState is not null after data retrieval
        assertNotNull(viewModel.uiState.value.listensTabUiState)

        // Assert that the data loaded belongs to the user themselves
        assertEquals(true, viewModel.uiState.value.isSelf)

        // Verify the number of similar users loaded is as expected (3 in this case)
        assertEquals(3, viewModel.uiState.value.listensTabUiState.similarUsers?.size)

        // Confirm the listen count is accurate after data loading
        assertEquals(3252, viewModel.uiState.value.listensTabUiState.listenCount)

        // Validate that the first follower's username is correctly loaded
        assertEquals("jivteshs20", viewModel.uiState.value.listensTabUiState.followers?.get(0)?.first)

        // Check that the correct MusicBrainz Identifier (MBID) for a loved song is loaded
        assertEquals("6b08f3d4-0d56-406c-b628-d0afe2ad5d44", viewModel.uiState.value.tasteTabUIState.lovedSongs?.feedback?.get(0)?.recordingMBID)

        // Ensure that the user listening activity data for all time is loaded with the correct size
        assertEquals(23, viewModel.uiState.value.statsTabUIState.userListeningActivity.get(Pair(UserGlobal.USER, StatsRange.ALL_TIME))?.size)
    }
}
