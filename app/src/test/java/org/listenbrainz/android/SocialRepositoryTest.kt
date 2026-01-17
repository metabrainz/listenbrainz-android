package org.listenbrainz.android

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SocialResponse
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.ErrorUtil.userNotFoundError
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowersSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowingSuccessData
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testFollowUnfollowSuccessResponse
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSearchResult
import org.listenbrainz.sharedtest.testdata.SocialRepositoryTestData.testSimilarUserSuccessData
import org.listenbrainz.sharedtest.utils.AssertionUtils
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFamiliarUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUserDNE
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SocialRepositoryTest : BaseUnitTest() {
    
    @Mock
    private lateinit var mockRepository: SocialRepository
    
    @BeforeTest
    fun setUp() {
        // getFollowing mocks
        wheneverBlocking {
            mockRepository.getFollowing(testUsername)
        }.thenReturn(Resource.success(testFollowingSuccessData))
        
        wheneverBlocking {
            mockRepository.getFollowing(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist(actualResponse = userNotFoundError)))
        
        // getFollowers mocks
        wheneverBlocking {
            mockRepository.getFollowers(testUsername)
        }.thenReturn(Resource.success(testFollowersSuccessData))
        
        wheneverBlocking {
            mockRepository.getFollowers(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist(actualResponse = userNotFoundError)))
        
        // follow mocks
        wheneverBlocking {
            mockRepository.followUser(testSomeOtherUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
        
        wheneverBlocking {
            mockRepository.followUser(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist(actualResponse = userNotFoundError)))
        
        wheneverBlocking {
            mockRepository.followUser(testUsername)
        }.doReturn(Resource.failure(error = ResponseError.BadRequest(actualResponse = cannotFollowSelfError)))
        
        wheneverBlocking {
            mockRepository.followUser(testFamiliarUser)
        }.doReturn(Resource.failure(error = ResponseError.BadRequest(actualResponse = alreadyFollowingError)))
        
        // unfollow mocks
        wheneverBlocking {
            mockRepository.unfollowUser(testFamiliarUser)
        }.thenReturn(Resource.success(testFollowUnfollowSuccessResponse))
        
        // getSimilarUsers mocks
        wheneverBlocking {
            mockRepository.getSimilarUsers(testUsername)
        }.thenReturn(Resource.success(testSimilarUserSuccessData))
        
        wheneverBlocking {
            mockRepository.getSimilarUsers(testUserDNE)
        }.doReturn(Resource.failure(error = ResponseError.DoesNotExist(actualResponse = userNotFoundError)))
        
        // searchUser mocks
        wheneverBlocking {
            mockRepository.searchUser(testUsername)
        }.thenReturn(Resource.success(testSearchResult))
    }
    
    // getFollowing() tests
    
    @Test
    fun `test getFollowing() success response`() = runTest {
        val expected = testFollowingSuccessData
        val result = mockRepository.getFollowing(testUsername)
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        AssertionUtils.checkFollowingAssertions(result, expected)
    }
    
    @Test
    fun `test getFollowing() DNE error response`() = runTest {
        val result = mockRepository.getFollowing(testUserDNE)
        val data = result.data
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertNull(data?.following)
        assertNull(data?.user)
        assertTrue(result.error is ResponseError.DoesNotExist)
    }
    
    // getFollowers() tests
    
    @Test
    fun `test getFollowers() success response`() = runTest {
        val expected = testFollowersSuccessData
        val result = mockRepository.getFollowers(testUsername)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        AssertionUtils.checkFollowersAssertions(result, expected)
    }
    
    @Test
    fun `test getFollowers() error response`() = runTest {
        val result = mockRepository.getFollowers(testUserDNE)
        val data = result.data
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertNull(data?.followers)
        assertNull(data?.user)
        assertTrue(result.error is ResponseError.DoesNotExist)
        assertEquals(userNotFoundError, result.error?.actualResponse)
    }
    
    // follow() tests
    
    @Test
    fun `test follow() success response`() = runTest {
        val result = mockRepository.followUser(testSomeOtherUser)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals("ok", result.data?.status)
    }
    
    @Test
    fun `test follow() error responses`() = runTest {
        // User DNE
        var result = mockRepository.followUser(testUserDNE)
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertNull(result.data?.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
        assertEquals(userNotFoundError, result.error?.actualResponse)
        
        // Cannot follow self
        result = mockRepository.followUser(testUsername)
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertNull(result.data?.status)
        assertTrue(result.error is ResponseError.BadRequest)
        assertEquals(cannotFollowSelfError, result.error?.actualResponse)
        
        // Already following
        result = mockRepository.followUser(testFamiliarUser)
        assertEquals(Resource.Status.FAILED, result.status)
        assertNull(result.data?.status)
        assertTrue(result.error is ResponseError.BadRequest)
        assertEquals(alreadyFollowingError, result.error?.actualResponse)
    }
    
    // unfollow() tests
    
    @Test
    fun `test unfollow() success response`() = runTest {
        val result = mockRepository.unfollowUser(testFamiliarUser)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals("ok", result.data?.status)
    }
    
    // getSimilarUsers() tests
    
    @Test
    fun `test getSimilarUsers() success response`() = runTest {
        val result = mockRepository.getSimilarUsers(testUsername)
        val expected = testSimilarUserSuccessData
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertNull(result.error)
        assertEquals(expected.payload, result.data?.payload)
    }
    
    @Test
    fun `test getSimilarUsers() error response`() = runTest {
        val result = mockRepository.getSimilarUsers(testUserDNE)
    
        assertEquals(Resource.Status.FAILED, result.status)
        assertTrue(result.error is ResponseError.DoesNotExist)
        assertNull(result.data?.payload)
        assertEquals(userNotFoundError, result.error?.actualResponse)
    }
    
    // searchUser() tests
    
    @Test
    fun `test searchUser() success response`() = runTest {
        val result = mockRepository.searchUser(testUsername)
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(testSearchResult, result.data)
    }
}
