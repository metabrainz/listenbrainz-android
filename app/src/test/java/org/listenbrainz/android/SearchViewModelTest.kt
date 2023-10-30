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
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.SearchViewModel
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowUnfollowSuccessResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSearchResult
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFamiliarUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
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
        
        // Unfollow some other user mock
        wheneverBlocking {
            mockSocialRepository.unfollowUser(testSomeOtherUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
    
        // User tries to follow some unknown user.
        wheneverBlocking {
            mockSocialRepository.followUser(testSomeOtherUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
    
        // User tries to follow an already followed user.
        wheneverBlocking {
            mockSocialRepository.followUser(testFamiliarUser)
        }.doReturn(Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = alreadyFollowingError }))
        
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
    
        // User tries to follow self.
        wheneverBlocking {
            mockSocialRepository.followUser(testUsername)
        }.doReturn(Resource.failure(error = ResponseError.BAD_REQUEST.apply { actualResponse = cannotFollowSelfError }))
        
        makeQueryAndAssert()
            .toggleFollowStatus(testUsername, this)
            // Expected is false here because testUsername is user itself.
            .assertFollowStatusChanged(testUsername, expected = false)
            .assertErrorFlowChanged()
    }
    
    @Test
    fun `test if state remains unchanged on already following error and error flow is updated`() = test {
        makeQueryAndAssert()
            .toggleFollowStatus(testFamiliarUser, this)
            // Expected is true here even though initial state is false because testFamiliarUser is already being followed
            // and our UI states should remain true to actual data.
            .assertFollowStatusChanged(testFamiliarUser, expected = true)
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
    
    private fun toggleFollowStatus (user: String, testScope: TestScope) : SearchViewModelTest {
        testScope.advanceUntilIdle()
        viewModel.toggleFollowStatus(User(user), getIndex(user))
        return this
    }
    
    private fun assertFollowStatusChanged(user: String, expected: Boolean) : SearchViewModelTest {
        val index = getIndex(user)
        assertEquals(user, viewModel.uiState.value.result.userList[index].username)
        assertEquals(expected, viewModel.uiState.value.result.isFollowedList[index])
        return this
    }
    
    private fun assertErrorFlowChanged() : SearchViewModelTest {
        assertEquals(viewModel.uiState.value.error, ResponseError.BAD_REQUEST)
        return this
    }
    
    private fun getIndex(user: String): Int {
        return when(user){
            testUsername -> 0
            testFamiliarUser -> 1
            testSomeOtherUser -> 2
            else -> throw IndexOutOfBoundsException()
        }
    }
}