package org.listenbrainz.android.yim

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.BaseUnitTest
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.YimViewModel
import org.listenbrainz.sharedtest.mocks.MockYimRepository
import org.listenbrainz.sharedtest.testdata.YimRepositoryTestData.testYimData
import org.listenbrainz.sharedtest.utils.AssertionUtils.checkYimAssertions
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class YimViewModelTest: BaseUnitTest() {
    
    private lateinit var viewModel : YimViewModel
    
    @Mock
    private lateinit var mockAppPreferences: AppPreferences
    
    @Mock
    private lateinit var mockYimRepository: MockYimRepository
    
    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup(){
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = YimViewModel(mockYimRepository, mockAppPreferences)
    }
    
    @Test
    fun getDataTest() = test {
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