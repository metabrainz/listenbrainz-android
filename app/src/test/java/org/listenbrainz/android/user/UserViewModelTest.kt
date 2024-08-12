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
        viewModel.getUserDataFromRemote(testUsername)
        // Ensure all coroutines and tasks are completed
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.statsTabUIState)
        assertNotNull(viewModel.uiState.value.tasteTabUIState)
        assertNotNull(viewModel.uiState.value.listensTabUiState)
        assertEquals(true, viewModel.uiState.value.isSelf)
        assertEquals(3, viewModel.uiState.value.listensTabUiState.similarUsers?.size)
        assertEquals(3252, viewModel.uiState.value.listensTabUiState.listenCount)
        assertEquals("jivteshs20", viewModel.uiState.value.listensTabUiState.followers?.get(0)?.first)
        assertEquals("6b08f3d4-0d56-406c-b628-d0afe2ad5d44", viewModel.uiState.value.tasteTabUIState.lovedSongs?.feedback?.get(0)?.recordingMBID)
        assertEquals(23,viewModel.uiState.value.statsTabUIState.userListeningActivity.get(Pair(UserGlobal.USER, StatsRange.ALL_TIME))?.size)
    }


}
