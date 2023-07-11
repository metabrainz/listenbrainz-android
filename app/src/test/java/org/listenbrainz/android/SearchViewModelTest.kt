package org.listenbrainz.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.SearchViewModel
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowUnfollowSuccessResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSearchResult
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.wheneverBlocking


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest : BaseUnitTest() {
    
    private lateinit var viewModel : SearchViewModel
    
    @Mock
    private lateinit var mockSocialRepository: SocialRepository
    
    @Before
    fun setup(){
        
        // Search response mock
        wheneverBlocking {
            mockSocialRepository.searchUser(testUsername)
        }.thenReturn(Resource.success(testSearchResult))
        
        // Follow user mock
        wheneverBlocking {
            mockSocialRepository.followUser(testSomeOtherUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
    
        // Unfollow user mock
        wheneverBlocking {
            mockSocialRepository.unfollowUser(testSomeOtherUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
        
        // Follow familiar user mock
        wheneverBlocking {
            mockSocialRepository.followUser(testUsername)
        }.thenReturn(Resource.failure(error = ResponseError.NETWORK_ERROR))
        
        viewModel = SearchViewModel(mockSocialRepository, testDispatcher(), testDispatcher())
    }
    
    @Test
    fun `test if query is updated`() = test {
        makeQueryAndAssert()
    }
    
    @Test
    fun `test if query is cleared`() = test {
        makeQueryAndAssert()
        advanceUntilIdle()
        clearQueryAndAssert()
    }
    
    @Test
    fun `test if follow or unfollow request is executed correctly`() = test {
        makeQueryAndAssert()
            .toggleFollowStatus(testSomeOtherUser, this)
            .assertFollowStatusChanged(testSomeOtherUser, expected = true)
            .toggleFollowStatus(testSomeOtherUser,this)
            .assertFollowStatusChanged(testSomeOtherUser, expected = false)
    }
    
    @Test
    fun `test error flow is updated`() = test {
        makeQueryAndAssert()
            .toggleFollowStatus(testUsername, this)
            // Expected is false here because testFamiliarUser is already being followed.
            .assertFollowStatusChanged(testUsername, expected = false)
            .assertErrorFlowChanged()
    }
    
    private fun makeQueryAndAssert() : SearchViewModelTest {
        viewModel.updateQueryFlow(testUsername)
        assertEquals(viewModel.uiState.value.query, testUsername)
        return this
    }
    
    private fun clearQueryAndAssert() : SearchViewModelTest {
        viewModel.clearUi()
        assertEquals(viewModel.uiState.value.query, "")
        return this
    }
    
    private suspend fun toggleFollowStatus(user: String, testScope: TestScope) : SearchViewModelTest {
        testScope.advanceUntilIdle()
        // Assigned values: 2 -> "AnotherUser", 0 -> "Jasjeet"
        viewModel.toggleFollowStatus(User(user), if (user == testSomeOtherUser) 2 else 0)
        return this
    }
    
    private fun assertFollowStatusChanged(user: String, expected: Boolean) : SearchViewModelTest {
        val index = if (user == testSomeOtherUser) 2 else 0
        assertEquals(user, viewModel.uiState.value.result.userList[index].username)
        assertEquals(expected, viewModel.uiState.value.result.isFollowedList[index])
        return this
    }
    
    private fun assertErrorFlowChanged() : SearchViewModelTest {
        assertEquals(viewModel.uiState.value.error, ResponseError.NETWORK_ERROR)
        return this
    }
    
}