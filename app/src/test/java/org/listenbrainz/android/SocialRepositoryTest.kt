package org.listenbrainz.android

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.repository.SocialRepositoryImpl
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.ResponseError
import org.listenbrainz.sharedtest.utils.AssertionUtils
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.alreadyFollowingError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.authHeaderNotFoundError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.cannotFollowSelfError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.ErrorUtil.userNotFoundError
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAccessToken
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAuthHeader
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFamiliarUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFollowersSuccessData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testFollowingSuccessData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSearchResult
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSimilarUserSuccessData
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testSomeOtherUser
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUserDNE
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername
import org.listenbrainz.sharedtest.utils.ResourceString.auth_header_not_found_error
import org.listenbrainz.sharedtest.utils.ResourceString.cannot_follow_self_error
import org.listenbrainz.sharedtest.utils.ResourceString.follow_error_response
import org.listenbrainz.sharedtest.utils.ResourceString.followers_response
import org.listenbrainz.sharedtest.utils.ResourceString.following_response
import org.listenbrainz.sharedtest.utils.ResourceString.search_response
import org.listenbrainz.sharedtest.utils.ResourceString.similar_users_response
import org.listenbrainz.sharedtest.utils.ResourceString.status_ok
import org.listenbrainz.sharedtest.utils.ResourceString.user_does_not_exist_error
import org.listenbrainz.sharedtest.utils.RetrofitUtils

class SocialRepositoryTest {
    
    private lateinit var webServer: MockWebServer
    private lateinit var repository: SocialRepository
    
    @Before
    fun setUp() {
        webServer = MockWebServer()
        webServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    
                    // Followers
                    
                    "/user/${testUsername}/followers" -> return MockResponse().setResponseCode(200).setBody(
                        followers_response
                    )
    
                    "/user/${testUserDNE}/followers" -> return MockResponse().setResponseCode(404).setBody(
                        user_does_not_exist_error
                    )
                    
                    // Following
                    
                    "/user/${testUsername}/following" -> return MockResponse().setResponseCode(200).setBody(
                        following_response
                    )
                    
                    "/user/${testUserDNE}/following" -> return MockResponse().setResponseCode(404).setBody(
                        user_does_not_exist_error
                    )
                    
                    // Follow
                    
                    "/user/${testFamiliarUser}/follow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader){
                            setResponseCode(400).setBody(
                                follow_error_response
                            )
                        }else {
                            setResponseCode(401).setBody(
                                auth_header_not_found_error
                            )
                        }
                    }
                    
                    "/user/${testSomeOtherUser}/follow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader){
                            setResponseCode(201).setBody(
                                status_ok
                            )
                        }else {
                            setResponseCode(401).setBody(
                                auth_header_not_found_error
                            )
                        }
                    }
    
                    "/user/${testUsername}/follow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader) {
                            setResponseCode(400).setBody(
                                cannot_follow_self_error
                            )
                        } else {
                            setResponseCode(401).setBody(
                                auth_header_not_found_error
                            )
                        }
                    }
    
                    "/user/$testUserDNE/follow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader){
                            setResponseCode(404).setBody(
                                user_does_not_exist_error
                            )
                        }else {
                            setResponseCode(401).setBody(
                                auth_header_not_found_error
                            )
                        }
                    }
                    
                    // Unfollow
                    
                    "/user/${testFamiliarUser}/unfollow" -> return MockResponse().apply {
                        if (request.getHeader("Authorization") == testAuthHeader) {
                            setResponseCode(201).setBody(
                                status_ok
                            )
                        } else {
                            setResponseCode(401).setBody(
                                auth_header_not_found_error
                            )
                        }
                    }
                    
                    // Similar users
    
                    "/user/$testUsername/similar-users" -> return MockResponse().setResponseCode(200).setBody(
                        similar_users_response
                    )
                    
                    "/user/$testUserDNE/similar-users" -> return MockResponse().setResponseCode(404).setBody(
                        user_does_not_exist_error
                    )
                    
                    // Search user
                    
                    "/search/users?search_term=$testUsername" -> { MockResponse().setResponseCode(200).setBody(
                            search_response
                        )
                    }
                    
                    "/search/users?search_term=$testFamiliarUser" -> { MockResponse().setResponseCode(429) }
    
                    else -> MockResponse().setResponseCode(400)
                    
                }
                
            }
        }
        webServer.start()
        val service = RetrofitUtils.createTestService(SocialService::class.java, webServer.url("/"))
        repository = SocialRepositoryImpl(service)
    }
    
    @After
    fun teardown() {
        webServer.close()
    }
    
    /* getFollowing() tests*/
    
    @Test
    fun `test getFollowing() success response`() = runTest {
        val expected = testFollowingSuccessData
        val result = repository.getFollowing(testUsername)
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        AssertionUtils.checkFollowingAssertions(result, expected)
    }
    
    @Test
    fun `test getFollowing() DNE error response`() = runTest {
        val result = repository.getFollowing(testUserDNE)
        val data = result.data
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null, data?.following)
        assertEquals(null, data?.user)
        assertEquals(ResponseError.USER_NOT_FOUND, result.error)
    }
    
    /* getFollowers() tests */
    
    @Test
    fun `test getFollowers() success response`() = runTest {
        val expected = testFollowersSuccessData
        val result = repository.getFollowers(testUsername)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        AssertionUtils.checkFollowersAssertions(result, expected)
    }
    
    @Test
    fun `test getFollowers() error response`() = runTest {
        // User DNE
        val result = repository.getFollowers(testUserDNE)
        val data = result.data
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null, data?.followers)
        assertEquals(null, data?.user)
        assertEquals(ResponseError.USER_NOT_FOUND, result.error)
        assertEquals(userNotFoundError, result.error?.actualResponse)
    }
    
    /* follow() tests */
    
    @Test
    fun `test follow() success response`() = runTest {
        val result = repository.followUser(testSomeOtherUser, accessToken = testAccessToken)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(result.data?.status, "ok")
    }
    
    @Test
    fun `test follow() error responses`() = runTest {
        // User DNE
        var result = repository.followUser(testUserDNE, accessToken = testAccessToken)
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.USER_NOT_FOUND, result.error)
        assertEquals(userNotFoundError, result.error?.actualResponse)
        
        // Cannot follow self
        result = repository.followUser(testUsername, accessToken = testAccessToken)
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.CANNOT_FOLLOW_SELF, result.error)
        assertEquals(cannotFollowSelfError, result.error?.actualResponse)
        
        // Already following
        result = repository.followUser(testFamiliarUser, accessToken = testAccessToken)
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.ALREADY_FOLLOWING, result.error)
        assertEquals(alreadyFollowingError, result.error?.actualResponse)
        
        // No Auth Header
        result = repository.followUser(testFamiliarUser, accessToken = "")     // Token is empty.
    
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.AUTH_HEADER_NOT_FOUND, result.error)
        assertEquals(authHeaderNotFoundError, result.error?.actualResponse)
    }
    
    /* unfollow() tests */
    
    @Test
    fun `test unfollow() success response`() = runTest {
        val result = repository.unfollowUser(testFamiliarUser, accessToken = testAccessToken)
    
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(result.data?.status, "ok")
    }
    
    @Test
    fun `test unfollow() error responses`() = runTest {
        // User DNE
        var result = repository.followUser(testUserDNE, accessToken = testAccessToken)
        
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.USER_NOT_FOUND, result.error)
        assertEquals(userNotFoundError, result.error?.actualResponse)
        
        // NOTE: Server does not send error response for when a user tries to unfollow themselves.
        
        // No Auth Header
        result = repository.followUser(testFamiliarUser, accessToken = "")     // Token is empty.
    
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(null ,result.data?.status)
        assertEquals(ResponseError.AUTH_HEADER_NOT_FOUND, result.error)
        assertEquals(authHeaderNotFoundError, result.error?.actualResponse)
    }
    
    @Test
    fun `test getSimilarUsers() success response`() = runTest {
        val result = repository.getSimilarUsers(testUsername)
        val expected = testSimilarUserSuccessData
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(null, result.error)
        assertEquals(expected.payload, result.data?.payload)
    }
    
    @Test
    fun `test getSimilarUsers() error response`() = runTest {
        // User DNE
        val result = repository.getSimilarUsers(testUserDNE)
    
        assertEquals(Resource.Status.FAILED, result.status)
        assertEquals(ResponseError.USER_NOT_FOUND, result.error)
        assertEquals(null, result.data?.payload)
        assertEquals(userNotFoundError, result.error?.actualResponse)
    }
    
    @Test
    fun `test searchUser() success response`() = runTest {
        val result = repository.searchUser(testUsername)
        
        assertEquals(Resource.Status.SUCCESS, result.status)
        assertEquals(testSearchResult, result.data)
    }
    
}