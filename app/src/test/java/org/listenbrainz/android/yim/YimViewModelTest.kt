package org.listenbrainz.android.yim

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.YimViewModel
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import org.listenbrainz.sharedtest.mocks.MockYimRepository
import org.listenbrainz.sharedtest.testdata.YimRepositoryTestData.testYimData
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions

class YimViewModelTest{
    
    private lateinit var viewModel : YimViewModel
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup(){
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        viewModel = YimViewModel(
            MockYimRepository(),
            MockAppPreferences(),
            testDispatcher,
            testDispatcher
        )
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
    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }
}