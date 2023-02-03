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
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.mocks.MockYimRepository
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testYimData

class YimViewModelTest{
    
    private lateinit var viewModel : YimViewModel
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup(){
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = YimViewModel(MockYimRepository())
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getDataTest() = runTest {
        val expected = testYimData
        launch(Dispatchers.Main) {
            val resultResource = viewModel.yimData
            assertEquals(Resource.Status.SUCCESS, resultResource.value.status)
            checkYimAssertions(resultResource.value.data, expected)
        }
    }
    
    @ExperimentalCoroutinesApi
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
}