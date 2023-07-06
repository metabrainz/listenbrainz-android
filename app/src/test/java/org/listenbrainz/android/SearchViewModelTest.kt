package org.listenbrainz.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.viewmodel.SearchViewModel
import org.listenbrainz.sharedtest.mocks.MockSocialRepository
import org.listenbrainz.sharedtest.utils.EntityTestUtils
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest : BaseUnitTest() {
    
    private lateinit var viewModel : SearchViewModel
    
    @Mock
    private lateinit var appPreferences: AppPreferences
    
    @Before
    fun setup(){
        viewModel = SearchViewModel(MockSocialRepository(), appPreferences, testDispatcher())
    }
    
    @Test
    fun `test if query is updated`() = test {
        viewModel.updateQueryFlow(testUsername)
        assertEquals(viewModel.uiState.value.query, testUsername)
    }
    
    @Test
    fun `test if query is cleared`() = test {
        viewModel.updateQueryFlow(testUsername)
        
        advanceUntilIdle()
        viewModel.clearUi()
        assertEquals(viewModel.uiState.value.query, "")
    }
    
    @Test
    fun `test if follow or unfollow request is executed correctly`() = test {
        var flow = viewModel.toggleFollowStatus(User(EntityTestUtils.testSomeOtherUser), false)
        flow.collect {
            // Hardcoded success response for testing
            assertEquals(it, true)
        }
        
        flow = viewModel.toggleFollowStatus(User(EntityTestUtils.testSomeOtherUser), true)
        flow.collect{
            // Hardcoded error response for testing
            assertEquals(it, false)
        }
    }
    
    @Test
    fun `test it shows error`() = test {
        val flow = viewModel.toggleFollowStatus(User(EntityTestUtils.testSomeOtherUser), true)
        flow.collect {
            assertEquals(it, false)
        }
        
        advanceUntilIdle()
        assertEquals(viewModel.uiState.value.error, ResponseError.BAD_REQUEST)
    }
}