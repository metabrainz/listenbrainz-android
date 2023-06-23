package org.listenbrainz.android

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.YimViewModel
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import org.listenbrainz.sharedtest.mocks.MockYimRepository
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testYimData

class YimViewModelTest{
    
    private lateinit var viewModel : YimViewModel
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup(){
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = YimViewModel(MockYimRepository(), MockAppPreferences(username = testUsername, loginStatus = STATUS_LOGGED_IN))
    }
    
    @Test
    fun getDataTest() = runTest {
        val expected = testYimData
        launch(Dispatchers.Main) {
            val resultResource = viewModel.yimData
            assertEquals(Resource.Status.SUCCESS, resultResource.value.status)
            checkYimAssertions(expected, resultResource.value.data)
        }
    }
    
    @ExperimentalCoroutinesApi
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
}